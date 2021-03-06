package de.ironjan.arionav_fw.samples.campus.views

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.arionav.ArEnabledNavigationFragmentHost
import de.ironjan.arionav_fw.ionav.positioning.bluetooth.BluetoothPositionProvider
import de.ironjan.arionav_fw.ionav.positioning.gps.GpsPositionPositionProvider
import de.ironjan.arionav_fw.ionav.positioning.wifi.WifiPositionProvider
import de.ironjan.arionav_fw.ionav.util.Mailer
import de.ironjan.arionav_fw.ionav.util.PermissionHelper
import de.ironjan.arionav_fw.samples.campus.CampusSampleApplication
import de.ironjan.arionav_fw.samples.campus.R
import de.ironjan.arionav_fw.samples.campus.data.WifiPositioningProviderHardCodedValues
import de.ironjan.arionav_fw.samples.campus.util.PreferenceKeys
import de.ironjan.graphhopper.extensions_core.Coordinate
import kotlinx.android.synthetic.main.activity_main.*

// todo initialize spinner with level data
class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    PermissionHelper.PermissionHelperCallback,
    ArEnabledNavigationFragmentHost {

    private lateinit var developerMails: Array<String>

    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        requestPermissions()
        activateBluetoothIfMissing()


        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, main_drawer_layout,
            R.string.drawer_open,
            R.string.drawer_close
        )
        main_drawer_layout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        nav_view.setupWithNavController(navController)

        nav_view.setNavigationItemSelectedListener { navigateOnMenuItem(it) }
    }

    private val bluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = ContextCompat.getSystemService(this, BluetoothManager::class.java)
        bluetoothManager?.adapter
    }

    private fun activateBluetoothIfMissing() {
        bluetoothAdapter?.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, bluetoothRequestCode)
        }

    }


    // region permission handling

    private val cameraRequestCode: Int = 1
    private val locationRequestCode: Int = 2
    private val bluetoothRequestCode: Int = 3

    /** requests permissions. positioning service is initialized on callback with granted permissions */
    private fun requestPermissions() {
        PermissionHelper.requestPermission(this, CAMERA, cameraRequestCode)
        PermissionHelper.requestPermission(this, ACCESS_FINE_LOCATION, locationRequestCode)
    }

    override fun showRationale(requestCode: Int) {
        when (requestCode) {
            locationRequestCode ->
                showPermissionRational(R.string.permission_fine_location_rationale, ACCESS_FINE_LOCATION, locationRequestCode)
            cameraRequestCode ->
                showPermissionRational(R.string.permission_camera_rationale, CAMERA, cameraRequestCode)
        }
    }

    private fun showCameraRationale() = showPermissionRational(R.string.permission_camera_rationale, CAMERA, cameraRequestCode)

    @SuppressLint("WrongConstant")
    private fun showPermissionRational(rationaleResId: Int, permission: String, requestCode: Int) {
        Snackbar.make(main_drawer_layout, rationaleResId, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.ok) {
                requestPermissions(this, arrayOf(permission), requestCode)
            }
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (permissions.isEmpty()) return


        when (requestCode) {
            cameraRequestCode -> {
                if (grantResults.any { it == PERMISSION_DENIED }) {
                    showCameraRationale()
                }
            }
            locationRequestCode -> {
                if (grantResults.any { it == PERMISSION_DENIED }) {
                    showPermissionRational(R.string.permission_fine_location_rationale, ACCESS_FINE_LOCATION, locationRequestCode)
                }
                initializePositioningService()
            }
        }
        requestPermissions()
    }

    override fun permissionAlreadyGranted(requestCode: Int) {
        super.permissionAlreadyGranted(requestCode)
        when (requestCode) {
            locationRequestCode -> initializePositioningService()
        }
    }
    // endregion


    // region initializePositioningService
    private fun initializePositioningService() {
        val ionavContainer = (application as CampusSampleApplication).ionavContainer
        this.developerMails = ionavContainer.developerMails

        val positioningService = ionavContainer.positioningService

        positioningService.apply {
            removeProvider(GpsPositionPositionProvider.GPS_PROVIDER_NAME)
            removeProvider(WifiPositionProvider.WIFI_POSITIONING_PROVIDER)
            removeProvider(BluetoothPositionProvider.BLUETOOTH_PROVIDER_NAME)
        }

        val gpsPositionProvider = GpsPositionPositionProvider(this, lifecycle, positioningService)
        val wifiPositioningProvider = WifiPositionProvider(this, lifecycle, WifiPositioningProviderHardCodedValues.deviceMap, WifiPositioningProviderHardCodedValues.deviceNameMap)
        val bluetoothProvider = BluetoothPositionProvider(this, lifecycle, bluetoothDeviceMap)

        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return

        val isBluetoothEnabled = sharedPref.getBoolean(PreferenceKeys.ENABLED_BLUETOOTH, false)
        val isWifiEnabled = sharedPref.getBoolean(PreferenceKeys.ENABLED_WIFI, false)
        val isGpsEnabled = sharedPref.getBoolean(PreferenceKeys.ENABLED_GPS, true)

        val prioBluetooth = sharedPref.getInt(PreferenceKeys.PRIORITY_BLUETOOTH, 0)
        val prioWifi = sharedPref.getInt(PreferenceKeys.PRIORITY_WIFI, 1)
        val prioGps = sharedPref.getInt(PreferenceKeys.PRIORITY_GPS, 2)

        positioningService.apply {
            registerProvider(bluetoothProvider, isBluetoothEnabled)
            registerProvider(wifiPositioningProvider, isWifiEnabled)
            registerProvider(gpsPositionProvider, isGpsEnabled)

            setPriority(prioBluetooth, bluetoothProvider)
            setPriority(prioWifi, wifiPositioningProvider)
            setPriority(prioGps, gpsPositionProvider)
        }
    }

    // endregion

    // region navigation

    val navController
        get() = findNavController(R.id.nav_host_fragment)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return navigateOnMenuItem(item)
    }

    private fun navigateOnMenuItem(item: MenuItem): Boolean {
        main_drawer_layout.closeDrawers();

        if (item.itemId == R.id.mnuFeedback) {

            Mailer(developerMails).sendGeneralFeedback(this)
            return true
        }

        val destination = getDestinationFromMenuItem(item)
        if (destination == -1) {
            return super.onOptionsItemSelected(item)
        }

        return navigateToId(destination)
    }

    private fun getDestinationFromMenuItem(item: MenuItem): Int = when (item.itemId) {
        R.id.mnuSimpleMap -> R.id.arEnabledMapViewFragment
        R.id.mnuWifiAps -> R.id.nearbyWifiAps
        R.id.mnuBtBeacons -> R.id.nearbyBluetoothTokensFragment
        R.id.mnuPoiList -> R.id.poiListFragment
        R.id.mnuProviderConfig -> R.id.providerConfig
        R.id.mnuLocationHistory -> R.id.locationHistory
        else -> -1
    }

    override fun goToArNav() {
        navigateToId(R.id.arNavFragment)
    }

    override fun goToStartNavigation() {
        navigateToId(R.id.startNavFragment)
    }

    override fun goToMapNavigation() {
        navigateToId(R.id.startNavFragment)
    }

    override fun goToInstructions() {
        navigateToId(R.id.textNavigationFragment)
    }

    override fun goToFeedback() {
        navigateToId(R.id.feedbackFragment)
    }

    override fun goToMapView(clearNavigationStack: Boolean) {
        if (clearNavigationStack) {
            navController.popBackStack(R.id.arEnabledMapViewFragment, false)
        } else {
            navigateToId(R.id.arEnabledMapViewFragment)
        }
    }

    private fun navigateToId(destination: Int): Boolean {
        if (destination == navController.currentDestination?.id) return true

        navController.navigate(destination)

        return true
    }
    // endregion

    companion object {
        val bluetoothDeviceMap = mapOf(
            "00:CD:FF:00:37:40" to Coordinate(51.731695, 8.734756, 1.0), // BR512856
            "00:CD:FF:00:34:D7" to Coordinate(51.731843, 8.734929, 1.0), // BR513883
            "EC:CD:47:40:AD:DC" to Coordinate(0.0, 0.0, 100.0) // Miband
        )
    }
}
