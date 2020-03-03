package de.ironjan.arionav.sample

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav.ionav.PermissionHelper
import de.ironjan.arionav.ionav.positioning.IPositionProvider
import de.ironjan.arionav.ionav.positioning.MergedPositionProvider
import de.ironjan.arionav.ionav.positioning.PositioningProviderRegistry
import de.ironjan.arionav.ionav.positioning.bluetooth.BluetoothPositioningProviderImplementation
import de.ironjan.arionav.ionav.positioning.gps.GpsPositionProvider
import de.ironjan.arionav.ionav.positioning.wifi.WifiPositioningProvider
import de.ironjan.arionav.sample.util.Mailer
import kotlinx.android.synthetic.main.activity_main.*

// todo initialize spinner with level data
class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    PermissionHelper.PermissionHelperCallback {
    private lateinit var mergedPositionProvider: MergedPositionProvider
    private lateinit var _positionProvider: IPositionProvider
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private val cameraRequestCode: Int = 1

    private val locationRequestCode: Int = 2

    private val bluetoothRequestCode: Int = 3

    var positionProvider: IPositionProvider
        get() = _positionProvider
        private set(value) {
            _positionProvider = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()
        activateBluetoothIfMissing()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, main_drawer_layout, R.string.drawer_open, R.string.drawer_close)
        main_drawer_layout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        nav_view.setupWithNavController(navController)

        nav_view.setNavigationItemSelectedListener { navigateOnMenuItem(it) }


        val providerRegistry = PositioningProviderRegistry.Instance

        val gpsPositionProvider = GpsPositionProvider(this, lifecycle)
        val wifiPositioningProvider = WifiPositioningProvider(this, lifecycle)
        val bluetoothProviderImplementation = BluetoothPositioningProviderImplementation(this, lifecycle)
        mergedPositionProvider = MergedPositionProvider(this, lifecycle)

        providerRegistry.registerProvider(gpsPositionProvider)
        providerRegistry.registerProvider(wifiPositioningProvider)
        providerRegistry.registerProvider(bluetoothProviderImplementation)

        mergedPositionProvider.addProvider(gpsPositionProvider)
        mergedPositionProvider.addProvider(wifiPositioningProvider)
        mergedPositionProvider.addProvider(bluetoothProviderImplementation)

        positionProvider = mergedPositionProvider

        positionProvider.start()

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

    val navController
        get() = findNavController(R.id.nav_host_fragment)


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

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
        // FIXME optimize: navigate only if destination!=location
        return when (item.itemId) {
            R.id.mnuMap -> {
                navController.navigate(R.id.action_to_mapFragment)
                true
            }
            R.id.mnuWifiAps -> {
                navController.navigate(R.id.action_to_nearbyWifiApsFragment)
                true
            }
            R.id.mnuBtBeacons -> {
                navController.navigate(R.id.action_to_nearbyBluetoothTokensFragment)
                true
            }
            R.id.mnuPoiList -> {
                navController.navigate(R.id.action_to_poiListFragment)
                true
            }
            R.id.mnuProviderConfig -> {
                navController.navigate(R.id.action_to_providerConfig)
                true
            }
            R.id.mnuFeedback -> {
                Mailer.sendFeedback(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


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
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mergedPositionProvider.start()
    }
    override fun onPause() {
        super.onPause()
        mergedPositionProvider.stop()
    }

}
