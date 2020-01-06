package de.ironjan.arionav.sample

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.android.material.snackbar.Snackbar
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import de.ironjan.arionav.ionav.GhzExtractor
import de.ironjan.arionav.ionav.LoadGraphTask
import de.ironjan.arionav.ionav.MapView
import de.ironjan.arionav.ionav.OsmBoundsExtractor
import de.ironjan.graphhopper.extensions_core.Coordinate
import de.ironjan.graphhopper.levelextension.Routing
import kotlinx.android.synthetic.main.activity_main.*
import org.oscim.android.canvas.AndroidGraphics
import org.oscim.core.GeoPoint
import org.oscim.event.Gesture
import org.oscim.event.GestureListener
import org.oscim.event.MotionEvent
import org.oscim.layers.Layer
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.layers.tile.buildings.BuildingLayer
import org.oscim.layers.tile.vector.labeling.LabelLayer
import org.oscim.layers.vector.geometries.Style
import org.oscim.theme.VtmThemes
import org.oscim.tiling.source.mapfile.MapFileTileSource
import org.slf4j.LoggerFactory
import java.util.ArrayList

// todo initialize spinner with level data
class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    private var selectedLevel: Double = 0.0
    val logger = LoggerFactory.getLogger("MainActivity")

    private val cameraRequestCode: Int = 1

    private val locationRequestCode: Int = 2
    private val ghzResId = ArionavSampleApplication.ghzResId
    private val mapName = ArionavSampleApplication.mapName

    private lateinit var ghzExtractor: GhzExtractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(this)

        // todo move to Application class?
        ghzExtractor = GhzExtractor(this, ghzResId, mapName)

        val mapEventsCallback = object : MapView.MapEventsCallback {
            override fun startPointCleared() {
                edit_start_coordinates.setText("")
            }

            override fun endPointCleared() {
                edit_end_coordinates.setText("")
            }

            override fun startPointChanged(coordinate: Coordinate) {
                edit_start_coordinates.setText(coordinate.asString() ?: "")
            }

            override fun endPointChanged(coordinate: Coordinate) {
                edit_end_coordinates.setText(coordinate.asString() ?: "")
            }
        }
        mapView.initialize(ghzExtractor, mapEventsCallback)

        buttonToggleLocation.setOnClickListener {
            // TODO
        }


        val levelList = listOf(-2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0)
        spinnerLevel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, levelList)
        spinnerLevel.setSelection(4)
        spinnerLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                setSelectedLevel(levelList[4])
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                setSelectedLevel(levelList[p2])
            }

        }
    }

    private fun setSelectedLevel(lvl: Double) {
        selectedLevel = lvl
        mapView.selectedLevel = lvl
    }



    private fun requestPermissions(activity: Activity) {
//        requestCameraPermission(activity)
//        requestLocationPermission(activity)
    }

    private fun requestLocationPermission(activity: Activity) {
        if (isPermissionGranted(ACCESS_FINE_LOCATION)) {
            logger.debug("Location permissions are already granted.")
        } else {
            if (shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)
            ) {
                logger.info("Displaying fine location permission rationale to provide additional context.")
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
            logger.debug("Camera permission is already granted.")
        } else {
            if (shouldShowRequestPermissionRationale(activity, CAMERA)) {
                logger.info("Displaying camera permission rationale to provide additional context.")
                showCameraRationale()
            } else {
                logger.info("Requesting camera permission.")
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
}
