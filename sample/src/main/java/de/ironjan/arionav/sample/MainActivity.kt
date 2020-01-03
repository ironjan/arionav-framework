package de.ironjan.arionav.sample

import android.Manifest.permission.*
import android.app.Activity
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
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val cameraRequestCode: Int = 1
    private val locationRequestCode: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(this)

        unzipGhzToStorage(R.raw.saw, "saw-gh")
    }

    private fun unzipGhzToStorage(resId: Int, folderName: String) {
        val targetFolderPath = "${filesDir.absolutePath}/$folderName/"
        val targetFolder = File(targetFolderPath)
        targetFolder.mkdirs()

        // check if unzipped ghz exists
        val timestampFileName = "_timestamp"

        val timestampFile = File(targetFolderPath + timestampFileName)
        val doesUnzipGhzExist = timestampFile.exists()

        // get the timestamp of the existing unzipped ghz file
        var timestampInExtracted: String? = null
        if (doesUnzipGhzExist) {
            Log.d(TAG, "Target folder for ghz file exists. Reading the existing timestamp...")
            FileInputStream(timestampFile).use {fileInputStream ->
                InputStreamReader(fileInputStream).use {inputStreamReader ->
                    BufferedReader(inputStreamReader).use {bufferedReader ->
                        timestampInExtracted = bufferedReader.readLine()
                        Log.d(TAG, "Read timestamp from extracted file: $timestampInExtracted")
                    }
                }
            }
        }

        // there is an unzipped ghz file. verify that it is the one contained in this sample.
        var timestampInGhz: String? = ""
        if (timestampInExtracted != null) {
            Log.d(TAG, "Extracting timestamp from zip file.")
            // compare with timestamp in zip
            resources.openRawResource(resId).use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    var zipEntry: ZipEntry? = zipInputStream.nextEntry
                    while (zipEntry != null) {
                        Log.d(TAG, "Processing ${zipEntry.name}...")
                        if (timestampFileName == zipEntry.name) {
                            // extract timestamp from zip and compare
                            Log.d(TAG, "Extract timestamp for comparison.")
                            val buffer = ByteArray(1024)
                            val targetFile = "${targetFolder.absolutePath}/_timestamp_tmp"
                            FileOutputStream(targetFile).use { fileOutputStream ->
                                var count = zipInputStream.read(buffer)
                                while (count != -1) {
                                    Log.d(TAG, "Count: $count")
                                    fileOutputStream.write(buffer, 0, count)
                                    count = zipInputStream.read(buffer)
                                }
                            }
                            Log.d(TAG, "Extracted timestamp entry to $targetFile")
                            FileInputStream(targetFile).use { fileInputStream ->
                                InputStreamReader(fileInputStream).use { inputStreamReader ->
                                    BufferedReader(inputStreamReader).use { bufferedReader ->
                                        timestampInGhz = bufferedReader.readLine()
                                        Log.d(TAG, "Extracted timestamp: $timestampInGhz")
                                    }
                                }
                            }
                            File(targetFile).deleteOnExit()
                            Log.d(TAG, "Deleting $targetFile")
                            zipEntry = null
                        } else {
                            Log.d(TAG, "Skipping entry")
                            zipInputStream.closeEntry()
                            zipEntry = zipInputStream.nextEntry
                        }

                    }
                }
            }
        }

        if (timestampInGhz == timestampInExtracted) {
            Log.d(TAG, "Timestamps do match and are both at $timestampInGhz. Skipping unzip.")
//            return
        }

        Log.d(TAG, "Timestamps do not match: $timestampInExtracted in extracted, $timestampInGhz in ghz file. Extracting zip.")
        resources.openRawResource(resId).use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var zipEntry: ZipEntry? = zis.nextEntry
                while (zipEntry != null) {
                    // ghz files are flat
                    val fileName = zipEntry?.name

                    val targetFile = "$targetFolderPath$fileName"

                    Log.d(TAG, "Unzipping ghz resource $folderName. Unzipping file $fileName  to $targetFile.")
                    val buffer = ByteArray(1024)
                    FileOutputStream(targetFile).use { fileOutputStream ->
                        var count = zis.read(buffer)
                        while (count != -1) {
                            fileOutputStream.write(buffer, 0, count)
                            count = zis.read(buffer)
                        }
                    }

                    zis.closeEntry()
                    zipEntry = zis.nextEntry
                }
                Log.d(TAG, "Completed unzip.")
            }
        }
    }

    private fun doActualUnzip(resId: Int, targetFolder: String, folderName: String) {
        val inputStream = resources.openRawResource(resId)

        inputStream.use {
            val zipInputStream = ZipInputStream(inputStream)
            zipInputStream.use {
                var zipEntry: ZipEntry? = it.nextEntry
                while (zipEntry != null) {
                    // ghz files are flat
                    val fileName = zipEntry.name

                    val targetFile = "$targetFolder$fileName"

                    Log.d(TAG, "Unzipping ghz resource $folderName. Unzipping file $fileName  to $targetFile.")
                    // TODO write unzipped file
                    //                    FileOutputStream(targetFile)

                    zipEntry = it.nextEntry
                }
            }
        }
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
