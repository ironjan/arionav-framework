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


    fun requestPermission(permission: String, requestCode: Int) {
        if (isPermissionGranted(permission)) {
            logger.debug("Location permissions are already granted.")
        } else {
            if (shouldShowRequestPermissionRationale(permission)
            ) {
                logger.info("Displaying fine location permission rationale to provide additional context.")
                callback.showRationale(requestCode)
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
            }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRequestPermissionRationale(permission: String): Boolean = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    companion object {
        const val TAG = "PermissionHelper"
    }

    interface PermissionHelperCallback  {
        fun showRationale(requestCode: Int)
    }

}