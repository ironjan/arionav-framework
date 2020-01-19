package de.ironjan.arionav.ionav

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.slf4j.LoggerFactory

class PermissionHelper private constructor() {

    companion object {
        const val TAG = "PermissionHelper"
        private val logger = LoggerFactory.getLogger(TAG)

        fun <T> requestPermission(activity: T, permission: String, requestCode: Int)
                where T : Activity, T : PermissionHelperCallback {
            if (isPermissionGranted(activity,permission)) {
                logger.debug("Location permissions are already granted.")
            } else {
                if (shouldShowRequestPermissionRationale(activity,permission)) {
                    logger.info("Displaying fine location permission rationale to provide additional context.")
                    activity.showRationale(requestCode)
                } else {
                    ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
                }
            }
        }

        private fun <T> isPermissionGranted(activity: T,permission: String): Boolean
                where T : Activity, T : PermissionHelperCallback
                = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

        private fun <T> shouldShowRequestPermissionRationale(activity: T,permission: String): Boolean
                where T : Activity, T : PermissionHelperCallback
                = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    }

    interface PermissionHelperCallback {
        fun showRationale(requestCode: Int)
    }


}