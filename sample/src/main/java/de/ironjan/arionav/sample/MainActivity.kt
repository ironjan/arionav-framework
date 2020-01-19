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
import com.google.android.material.snackbar.Snackbar
import com.graphhopper.PathWrapper
import de.ironjan.arionav.framework.PathWrapperJsonConverter
import de.ironjan.arionav.ionav.*
import de.ironjan.graphhopper.extensions_core.Coordinate
import kotlinx.android.synthetic.main.activity_main.*
import org.slf4j.LoggerFactory

// todo initialize spinner with level data
class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    PermissionHelper.PermissionHelperCallback {


    private var displayedRoute: PathWrapper? = null
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

        requestPermissions()

        // todo move to Application class?
        ghzExtractor = GhzExtractor(this, ghzResId, mapName)

        val mapEventsCallback = object : MapView.MapEventsCallback {
            override fun onRouteShown(pathWrapper: PathWrapper) {
                displayedRoute = pathWrapper
                button_AR.isEnabled = true
            }

            override fun onRouteCleared() {
                displayedRoute = null
                button_AR.isEnabled = false
            }

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

        buttonCenterOnPos.setOnClickListener {
            centerMapOnPosition()
        }

        button_AR.setOnClickListener(this::onArButtonClick)

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

        UserPositionListener(this, lifecycle) { location ->
            mapView.setUserPosition(Coordinate(location.latitude, location.longitude, 0.0))
        }

        mapView.setUserPosition(Coordinate(51.71858, 8.74905, 0.0))
    }

    private fun centerMapOnPosition() {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show()
        // FIXME Implement
        mapView.setUserPosition(Coordinate(51.71858, 8.74905, 0.0))
    }

    private fun onArButtonClick(it: View) {
        if (!it.isEnabled) {
            logger.warn("button_AR was clicked but is not enabled. Ignoring.")
            return
        }

        val lDisplayedRoute = displayedRoute
        if (lDisplayedRoute == null) {
            logger.info("AR button was clicked with null route. Ignoring.")
            // TODO show warning in ui
            return
        }

        logger.info("Starting AR activity with route {}", displayedRoute)
        // todo implement

        val i = Intent(this, ArViewActivity::class.java)

        val toSimplifiedRouteJson = PathWrapperJsonConverter.toSimplifiedRouteJson(lDisplayedRoute)
        i.putExtra("ROUTE", toSimplifiedRouteJson)
        startActivity(i);


    }

    private fun setSelectedLevel(lvl: Double) {
        selectedLevel = lvl
        mapView.selectedLevel = lvl
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
        Snackbar.make(mainLayout, rationaleResId, Snackbar.LENGTH_INDEFINITE)
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
}
