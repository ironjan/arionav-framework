package de.ironjan.arionav.sample

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.graphhopper.PathWrapper
import de.ironjan.arionav.framework.PathWrapperJsonConverter
import de.ironjan.arionav.ionav.*
import de.ironjan.arionav.ionav.positioning.gps.GpsPositionProvider
import de.ironjan.arionav.ionav.special_routing.model.Poi
import de.ironjan.arionav.ionav.special_routing.repository.RoomRepository
import de.ironjan.arionav.ionav.special_routing.model.Room
import de.ironjan.arionav.ionav.special_routing.repository.PoiRepository
import de.ironjan.graphhopper.extensions_core.Coordinate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_map.*
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import kotlin.math.round

// todo initialize spinner with level data
class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    PermissionHelper.PermissionHelperCallback {
    val logger = LoggerFactory.getLogger("MainActivity")
    private val cameraRequestCode: Int = 1

    private val locationRequestCode: Int = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        requestPermissions()


    }



    private fun requestPermissions() {
//        PermissionHelper.requestPermission(this, CAMERA, cameraRequestCode)
        PermissionHelper.requestPermission(this, ACCESS_FINE_LOCATION, cameraRequestCode)
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
        Snackbar.make(content, rationaleResId, Snackbar.LENGTH_INDEFINITE)
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

    object InstructionSignToText {
        fun getTextFor(sign: Int): String {
            return when (sign) {
                -99 -> "UNKNOWN"
                -98 -> "U_TURN_UNKNOWN"
                -8 -> "U_TURN_LEFT"
                -7 -> "KEEP_LEFT"
                -6 -> "LEAVE_ROUNDABOUT" // for future use
                -3 -> "TURN_SHARP_LEFT"
                -2 -> "TURN_LEFT"
                -1 -> "TURN_SLIGHT_LEFT"
                0 -> "CONTINUE_ON_STREET"
                1 -> "TURN_SLIGHT_RIGHT"
                2 -> "TURN_RIGHT"
                3 -> "TURN_SHARP_RIGHT"
                4 -> "FINISH"
                5 -> "REACHED_VIA"
                6 -> "USE_ROUNDABOUT"
                Integer.MIN_VALUE -> "IGNORE"
                7 -> "KEEP_RIGHT"
                8 -> "U_TURN_RIGHT"
                101 -> "PT_START_TRIP"
                102 -> "PT_TRANSFER"
                103 -> "PT_END_TRIP"
                else -> "UNKNOWN"
            }
        }
    }
}
