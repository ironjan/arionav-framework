package de.ironjan.arionav.sample

import android.Manifest
import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val cameraRequestCode: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(this)
    }

    companion object {
        const val TAG = "MainActivity"
    }

    private fun requestPermissions(activity: Activity) {
        requestCameraPermission(activity)
    }

    private fun requestCameraPermission(activity: Activity) {
        if (isPermissionGranted(CAMERA)) {
            Log.d(TAG, "Camera permission was already granted.")
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
                Log.i(TAG, "Displaying camera permission rationale to provide additional context.")
                Snackbar.make(
                    mainLayout, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.ok) {
                        requestPermission(this, CAMERA, cameraRequestCode)
                    }
                    .show()
            } else {
                requestPermission(activity, CAMERA, cameraRequestCode)
            }
        }
    }

    private fun requestPermission(activity: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            requestCode
        )
    }

    private fun isPermissionGranted(camera: String) =
        PackageManager.PERMISSION_GRANTED == checkSelfPermission(camera)

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {

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
