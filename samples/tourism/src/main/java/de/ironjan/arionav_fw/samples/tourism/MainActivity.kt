package de.ironjan.arionav_fw.samples.tourism

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.arionav.ArEnabledNavigationFragmentHost
import de.ironjan.arionav_fw.ionav.positioning.gps.GpsPositionPositionProvider
import de.ironjan.arionav_fw.ionav.util.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    PermissionHelper.PermissionHelperCallback,
    ArEnabledNavigationFragmentHost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        requestPermissions()
    }


    // region permission handling

    private val cameraRequestCode: Int = 1
    private val locationRequestCode: Int = 2

    /** requests permissions. positioning service is initialized on callback with granted permissions */
    private fun requestPermissions() {
        PermissionHelper.requestPermission(this, Manifest.permission.CAMERA, cameraRequestCode)
        PermissionHelper.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, locationRequestCode)
    }

    override fun showRationale(requestCode: Int) {
        when (requestCode) {
            locationRequestCode ->
                showPermissionRational(R.string.permission_fine_location_rationale, Manifest.permission.ACCESS_FINE_LOCATION, locationRequestCode)
            cameraRequestCode ->
                showPermissionRational(R.string.permission_camera_rationale, Manifest.permission.CAMERA, cameraRequestCode)
        }
    }

    private fun showCameraRationale() = showPermissionRational(R.string.permission_camera_rationale, Manifest.permission.CAMERA, cameraRequestCode)

    @SuppressLint("WrongConstant")
    private fun showPermissionRational(rationaleResId: Int, permission: String, requestCode: Int) {
        Snackbar.make(main_drawer_layout, rationaleResId, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.ok) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (permissions.isEmpty()) return


        when (requestCode) {
            cameraRequestCode -> {
                if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                    showCameraRationale()
                }
            }
            locationRequestCode -> {
                if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                    showPermissionRational(R.string.permission_fine_location_rationale, Manifest.permission.ACCESS_FINE_LOCATION, locationRequestCode)
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
        val positioningService = (application as TourismSampleApplication).ionavContainer.positioningService

        positioningService.removeProvider(GpsPositionPositionProvider.GPS_PROVIDER_NAME)

        val gpsPositionProvider = GpsPositionPositionProvider(this, lifecycle, positioningService)

        positioningService.registerProvider(gpsPositionProvider, true)
    }

    // endregion


    private val navController
        get() = findNavController(R.id.nav_host_fragment)


    override fun goToArNav() {
        navController.navigate(R.id.CustomArNavFragment)
    }

    override fun goToStartNavigation() {
        navController.navigate(R.id.startNavigationFragment)
    }

    override fun goToMapNavigation() {
        Toast.makeText(this, "going to map...", Toast.LENGTH_SHORT).show()
    }

    override fun goToInstrucitons() {
        navController.navigate(R.id.InstructionsListFragment)
    }

    override fun goToFeedback() {
        navController.navigate(R.id.feedbackFragment)
    }

    override fun goToMapView(clearNavigationStack: Boolean) {
        if (clearNavigationStack) {
            navController.popBackStack(R.id.arEnabledMapView, false)
        } else {
            navController.navigate(R.id.arEnabledMapView)
        }
    }

}
