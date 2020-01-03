package de.ironjan.arionav.sample

import android.Manifest.permission.*
import android.app.Activity
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val cameraRequestCode: Int = 1
    private val locationRequestCode: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(this)
    }

    companion object {
        const val TAG = "MainActivity"
    }

    private fun requestPermissions(activity: Activity) {
//        requestCameraPermission(activity)
//        requestLocationPermission(activity)
    }

    private fun requestLocationPermission(activity: Activity) {
        if (isPermissionGranted(ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "Location permissions are already granted.")
        } else {
            if (shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)
            ) {
                Log.i(TAG, "Displaying fine location permission rationale to provide additional context.")
                showFineLocationRationale()
            } else {
                requestPermissions(activity, arrayOf(ACCESS_FINE_LOCATION), locationRequestCode)
            }
        }
    }

    private fun showFineLocationRationale() = showPermissionRational(R.string.permission_fine_location_rationale, ACCESS_FINE_LOCATION, locationRequestCode)

    /**
     * TODO move to library
     */
    private fun requestCameraPermission(activity: Activity) {
        if (isPermissionGranted(CAMERA)) {
            Log.d(TAG, "Camera permission is already granted.")
        } else {
            if (shouldShowRequestPermissionRationale(activity, CAMERA)) {
                Log.i(TAG, "Displaying camera permission rationale to provide additional context.")
                showCameraRationale()
            } else {
                Log.i(TAG, "Requesting camera permission.")
                requestPermissions(activity, arrayOf(CAMERA), cameraRequestCode)
            }
        }
    }

    private fun showCameraRationale() = showPermissionRational(R.string.permission_camera_rationale, CAMERA, cameraRequestCode)

    private fun showPermissionRational(rationaleResId: Int, permission: String, requestCode: Int) {
        Snackbar.make(mainLayout, rationaleResId, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.ok) {
                requestPermissions(this, arrayOf(permission), requestCode)
            }
            .show()
    }

    private fun isPermissionGranted(permission: String) = checkSelfPermission(permission) == PERMISSION_GRANTED

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
                    showFineLocationRationale()
                }
            }
        }
    }

    object PermissionHelper {

        fun verifyPermissions(activity: Activity) {
            /*
            Your activity has to implement ActivityCompat.OnRequestPermissionsResultCallback and the results of permission requests will be delivered to its ActivityCompat.OnRequestPermissionsResultCallback.onRequestPermissionsResult(int, String[], int[]) method.
    Note that requesting a permission does not guarantee it will be granted and your app should be able to run without having this permission.
    This method may start an activity allowing the user to choose which permissions to grant and which to reject. Hence, you should be prepared that your activity may be paused and resumed. Further, granting some permissions may require a restart of you application. In such a case, the system will recreate the activity stack before delivering the result to your ActivityCompat.OnRequestPermissionsResultCallback.onRequestPermissionsResult(int, String[], int[]).
    When checking whether you have a permission you should use checkSelfPermission(Context, String).
    Calling this API for permissions already granted to your app would show UI to the user to decided whether the app can still hold these permissions. This can be useful if the way your app uses the data guarded by the permissions changes significantly.
    You cannot request a permission if your activity sets noHistory to true in the manifest because in this case the activity would not receive result callbacks including ActivityCompat.OnRequestPermissionsResultCallback.onRequestPermissionsResult(int, String[], int[]).
    The RuntimePermissions  sample app demonstrates how to use this method to re
             */
        }
    }
}
