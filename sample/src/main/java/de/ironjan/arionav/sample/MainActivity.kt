package de.ironjan.arionav.sample

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.android.material.snackbar.Snackbar
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import de.ironjan.arionav.ionav.GhzExtractor
import de.ironjan.arionav.ionav.LoadGraphTask
import de.ironjan.arionav.ionav.OsmBoundsExtractor
import de.ironjan.graphhopper.extensions_core.Coordinate
import de.ironjan.graphhopper.levelextension.Routing
import kotlinx.android.synthetic.main.activity_main.*
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
import org.oscim.theme.VtmThemes
import org.oscim.tiling.source.mapfile.MapFileTileSource
import org.slf4j.LoggerFactory
import org.slf4j.impl.HandroidLoggerAdapter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

// todo initialize spinner with level data
class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    val logger = LoggerFactory.getLogger("MainActivity")
    
    private var endCoordinate: Coordinate? = null
    private var startCoordinate: Coordinate? = null
    private var hopper: GraphHopper? = null
    private var isFollowGps: Boolean = false
    private val cameraRequestCode: Int = 1
    private val locationRequestCode: Int = 2

    private val ghzResId = R.raw.saw
    private val mapName = "saw"
    private lateinit var ghzExtractor: GhzExtractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(this)

        // todo move to Application class?
        ghzExtractor = GhzExtractor(this, ghzResId, mapName)

        loadMap()
        loadGraphStorage()

        buttonToggleLocation.setOnClickListener {
            // TODO
        }
    }

    private fun loadMap() {
        logger.debug("Loading map for map view")
        mapView!!.map().layers().add(MapEventsReceiver(mapView!!.map()))

        // Map file source
        val tileSource = MapFileTileSource()
        tileSource.setMapFile(ghzExtractor.mapFilePath)
        logger.debug("Set tile source to ${ghzExtractor.mapFilePath}")
        val l = mapView!!.map().setBaseMap(tileSource)
        mapView!!.map().setTheme(VtmThemes.DEFAULT)
        mapView!!.map().layers().add(BuildingLayer(mapView!!.map(), l))
        logger.debug("Added building layer")

        mapView!!.map().layers().add(LabelLayer(mapView!!.map(), l))
        logger.debug("Added label layer")

        var itemizedLayer: ItemizedLayer<MarkerItem>? = ItemizedLayer(mapView!!.map(), null as MarkerSymbol?)
        mapView!!.map().layers().add(itemizedLayer)
        logger.debug("Added marker layer")

        // Map position
        centerMap()
    }

    private fun centerMap() {
        val mapCenter = getCenterFromOsm(ghzExtractor.osmFilePath)
        mapView!!.map().setMapPosition(mapCenter.latitude, mapCenter.longitude, (1 shl 18).toDouble())
        logger.debug("Set map center to ${mapCenter.latitude}, ${mapCenter.longitude}")
    }

    private fun getCenterFromOsm(osmFilePath: String): GeoPoint {
        var readBoundsFromOsm: OsmBoundsExtractor.Bounds? = OsmBoundsExtractor.extractBoundsFromOsm(osmFilePath)

        if (readBoundsFromOsm != null) {

            val centerLat = (readBoundsFromOsm.minLat + readBoundsFromOsm.maxLat) / 2
            val centerLon = (readBoundsFromOsm.minLon + readBoundsFromOsm.maxLon) / 2

            return GeoPoint(centerLat, centerLon)
        }

        return GeoPoint(0, 0)
    }

    private fun loadGraphStorage() {
        logger.debug("loading graphstorage..")
        val loadGraphTask = LoadGraphTask(ghzExtractor.mapFolder, object : LoadGraphTask.Callback {
            override fun onSuccess(graphHopper: GraphHopper) {
                logger.debug("Completed loading graph.")
                hopper = graphHopper
            }

            override fun onError(exception: Exception) {
                logger.error("Error when loading graph: $exception")
                // FIXME show error
            }

        })
        loadGraphTask.execute()
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

                    logger.debug("Unzipping ghz resource $folderName. Unzipping file $fileName  to $targetFile.")
                    // TODO write unzipped file
                    //                    FileOutputStream(targetFile)

                    zipEntry = it.nextEntry
                }
            }
        }
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

    internal inner class MapEventsReceiver(map: org.oscim.map.Map) : Layer(map), GestureListener {

        override fun onGesture(g: Gesture, e: MotionEvent): Boolean {
            if (g is Gesture.LongPress) {
                val p = mMap.viewport().fromScreenPoint(e.x, e.y)
                return onLongPress(p)
            }

            logger.debug("Gesture: $g, MotionEvent: ${e.action}, ${e.x}, ${e.y}, count: ${e.pointerCount}, time: ${e.time}")
            return false
        }

        override fun onDetach() {
            super.onDetach()
            logger.debug("ondetach")
        }

    }

    private fun onLongPress(p: GeoPoint): Boolean {
        logger.debug("longpress at $p")
        if (hopper == null) {
            // FIXME show message
            logger.info("Graph not loaded yet. Ignoring long tap.")
            return false
        }

        if (startCoordinate != null && endCoordinate != null) {
            // clear start and end points
            setStartCoordinate(null)
            setEndCoordnate(null)
        }

        if (startCoordinate == null) {
            setStartCoordinate(p)
            logger.debug("Set start coordinate to $startCoordinate.")
            return true
        }

        if (endCoordinate == null) {
            setEndCoordnate(p)
            logger.debug("Set end coordinate to $endCoordinate.")
            computeAndShowRoute()
            return true
        }

        return true
    }

    private fun setStartCoordinate(p: GeoPoint?) {
        startCoordinate =
            if (p == null) null
            else Coordinate(p.latitude, p.longitude, 0.toDouble())

        edit_start_coordinates.setText(startCoordinate?.toString() ?: "")
    }

    private fun setEndCoordnate(p: GeoPoint?) {
        endCoordinate =
            if (p == null) null
            else Coordinate(p.latitude, p.longitude, 0.toDouble())

        edit_end_coordinates.setText(endCoordinate?.toString() ?: "")
    }

    private fun computeAndShowRoute() = showRoute(computeRoute())

    private fun showRoute(route: PathWrapper?) {
        if (route == null) {
            logger.info("Show route was called with a null route.")
            return
        }

        // FIXME show route
    }

    private fun computeRoute(): PathWrapper? {
        val lStartCoordinate = startCoordinate
        val lEndCoordinate = endCoordinate
        if (lStartCoordinate == null || lEndCoordinate == null) {
            logger.info("computeRoute was called with null for either start coordinate or end coordinate (start: $lStartCoordinate, end: $lEndCoordinate).")
            return null
        }

        try {
            val route = Routing(hopper).route(lStartCoordinate!!, lEndCoordinate!!)
            logger.debug("Computed route: $route")
            return route
        } catch (e: java.lang.Exception) {
            e.printStackTrace()

            return null
        }

    }

}
