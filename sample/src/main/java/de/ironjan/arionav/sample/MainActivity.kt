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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.graphhopper.PathWrapper
import de.ironjan.arionav.framework.PathWrapperJsonConverter
import de.ironjan.arionav.ionav.*
import de.ironjan.arionav.ionav.positioning.gps.GpsPositionProvider
import de.ironjan.arionav.ionav.res_helper.InstructionSignToText
import de.ironjan.graphhopper.extensions_core.Coordinate
import kotlinx.android.synthetic.main.activity_main.*
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import kotlin.math.round

// todo initialize spinner with level data
class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    PermissionHelper.PermissionHelperCallback {


    private lateinit var gpsPositionProvider: GpsPositionProvider

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

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        attachLifeCycleOwnerToMapView(lifecycleOwner)
        registerLiveDataObservers(lifecycleOwner)

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
            mapView.centerOnUserPosition()
        }

        buttonLocationAsStart.setOnClickListener {
            val coordinate = gpsPositionProvider.lastKnownPosition ?: return@setOnClickListener
            mapView.viewModel.setStartCoordinate(coordinate)
        }

        button_AR.setOnClickListener(this::onArButtonClick)

        val levelList = listOf(-2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0)


        gpsPositionProvider = GpsPositionProvider(this, lifecycle)
        gpsPositionProvider.start()


        mapView.viewModel.setUserPositionProvider(gpsPositionProvider)
        buttonMapFollowLocation.setOnClickListener { mapView.viewModel.toggleFollowUserPosition() }
        buttonRemainingRoute.setOnClickListener { mapView.viewModel.toggleShowRemainingRoute() }
    }

    private val viewModel
        get() = mapView.viewModel

    private fun registerLiveDataObservers(lifecycleOwner: LifecycleOwner) {
        viewModel.getStartCoordinateLifeData().observe(lifecycleOwner, Observer {
            edit_start_coordinates.setText(it?.asString() ?: "")
        })
        viewModel.getEndCoordinateLifeData().observe(lifecycleOwner, Observer {
            edit_end_coordinates.setText(it?.asString() ?: "")
        })
        viewModel.getFollowUserPositionLiveData().observe(lifecycleOwner, Observer {
            buttonMapFollowLocation.isChecked = it
        })
        viewModel.getShowRemainingRouteLiveData().observe(lifecycleOwner, Observer {
            buttonRemainingRoute.isChecked = it
        })
        viewModel.getLevelListLiveData().observe(lifecycleOwner, Observer {
            spinnerLevel.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, it)
        })
        viewModel.getSelectedLevelListPosition().observe(lifecycleOwner, Observer {
            spinnerLevel.setSelection(it)
        })
        viewModel.getNextInstructionLiveData().observe(lifecycleOwner, Observer {
            if (it == null) return@Observer
            if (viewModel.getShowRemainingRouteCurrentValue()) {

                val distance = round(it.distance * 100) / 100
                val instructionText = InstructionSignToText.getTextFor(it.sign)
                val timeInSeconds = it.time / 1000
                val timeInMinutes = timeInSeconds /60
                val msg = "$instructionText ${it.name}, ${distance}m, ${timeInMinutes}min"

                txtCurrentInstruction.setText(msg)
            }
        })

        spinnerLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
            }

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, value: Long) {
                viewModel.selectLevelListPosition(pos)
            }
        }

    }

    private fun attachLifeCycleOwnerToMapView(lifecycleOwner: LifecycleOwner) {
        mapView.onLifecycleOwnerAttached(lifecycleOwner)
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
