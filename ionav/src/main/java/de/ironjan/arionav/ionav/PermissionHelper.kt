package de.ironjan.arionav.ionav

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.slf4j.LoggerFactory

class PermissionHelper(private val activity: Activity ) {
    private var callback: PermissionHelperCallback
    private val logger = LoggerFactory.getLogger(TAG)

    init {
        require((activity is PermissionHelperCallback)) { "activity must implement PermissionHelperCallback" }
        require((activity is ActivityCompat.OnRequestPermissionsResultCallback)) { "activity must implement ActivityCompat.OnRequestPermissionsResultCallback" }
        callback = activity
    }

    fun isPermissionGranted(permission: String): Boolean = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    fun shouldShowRequestPermissionRationale(permission: String): Boolean = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    fun requestPermission(permission: String, requestCode: Int) = ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)

    fun requestLocationPermission(requestCode: Int) {
        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            logger.debug("Location permissions are already granted.")
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                logger.info("Displaying fine location permission rationale to provide additional context.")
                callback.showRationale(requestCode)
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, requestCode)
            }
        }
    }

    companion object {
        const val TAG = "PermissionHelper"
    }

    interface PermissionHelperCallback  {
        fun showRationale(requestCode: Int)
    }

}