package de.ironjan.arionav.ionav

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import de.ironjan.arionav.ionav.custom_view_mvvm.MvvmCustomView
import de.ironjan.arionav.ionav.mapview.MapViewState
import de.ironjan.arionav.ionav.mapview.MapViewViewModel
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.oscim.android.MapView
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

class MapView : MapView, MvvmCustomView<MapViewState, MapViewViewModel> {
    override val viewModel = MapViewViewModel()

    override fun onLifecycleOwnerAttached(lifecycleOwner: LifecycleOwner) {
        observeLiveData(lifecycleOwner)
    }

    private val endCoordinateMarker = R.drawable.marker_icon_red
    private val startCoordinateMarker = R.drawable.marker_icon_green
    private val currentUserPositionMarker = R.drawable.marker_icon_blue

    private fun observeLiveData(lifecycleOwner: LifecycleOwner) {
        viewModel.getStartCoordinateLifeData().observe(lifecycleOwner, Observer {

            updateMarkerLayer(startMarkerLayer, it, startCoordinateMarker)
            logger.info("Updated start coordinate in view to $it.")
        })

        viewModel.getEndCoordinateLifeData().observe(lifecycleOwner, Observer {
            updateMarkerLayer(endMarkerLayer, it, endCoordinateMarker)

            logger.debug("Updated end coordinate in view to $it.")
        })

        viewModel.getUserPositionLiveData().observe(lifecycleOwner, Observer {
            updateMarkerLayer(userPosLayer, it, currentUserPositionMarker)
        })
    }

    private fun updateMarkerLayer(layer: ItemizedLayer<MarkerItem>?, it: Coordinate?, marker: Int) {
        layer?.removeAllItems()
        redrawMap()

        if (it == null) return

        layer?.addItem(createMarkerItem(it, marker))
        redrawMap()
    }

    constructor(context: Context, attrsSet: AttributeSet) : super(context, attrsSet) {}

    constructor(context: Context) : super(context, null) {}

    private var isInitialized: Boolean = false
    private val notYetInitialized
        get() = !isInitialized


    private lateinit var mapEventsCallback: MapEventsCallback
    private lateinit var ghzExtractor: GhzExtractor

    private var routeLayer: org.oscim.layers.vector.PathLayer? = null
    var selectedLevel: Double = 0.0

    private var startMarkerLayer: ItemizedLayer<MarkerItem>? = null
    private var endMarkerLayer: ItemizedLayer<MarkerItem>? = null
    private var userPosLayer: ItemizedLayer<MarkerItem>? = null

    private val logger = LoggerFactory.getLogger(TAG)

    fun initialize(ghzExtractor: GhzExtractor, mapEventsCallback: MapEventsCallback) {
        this.ghzExtractor = ghzExtractor
        this.mapEventsCallback = mapEventsCallback
        loadMap()
        loadGraphStorage()
    }


    private fun loadGraphStorage() {
        logger.debug("loading graphstorage..")
        val loadGraphTask = LoadGraphTask(ghzExtractor.mapFolder, object : LoadGraphTask.Callback {
            override fun onSuccess(graphHopper: GraphHopper) {
                logger.debug("Completed loading graph.")
                viewModel.hopper = graphHopper
                isInitialized = true
            }

            override fun onError(exception: Exception) {
                logger.error("Error when loading graph: $exception")
                // FIXME show error
            }

        })
        loadGraphTask.execute()
    }

    private fun loadMap() {
        logger.debug("Loading map for map view")
        map().layers().add(MapEventsReceiver(map()))

        // Map file source
        val tileSource = MapFileTileSource()
        tileSource.setMapFile(ghzExtractor.mapFilePath)
        logger.debug("Set tile source to ${ghzExtractor.mapFilePath}")
        val l = map().setBaseMap(tileSource)
        map().setTheme(VtmThemes.DEFAULT)
        map().layers().add(BuildingLayer(map(), l))
        logger.debug("Added building layer")

        map().layers().add(LabelLayer(map(), l))
        logger.debug("Added label layer")

        startMarkerLayer = ItemizedLayer(map(), null as MarkerSymbol?)
        endMarkerLayer = ItemizedLayer(map(), null as MarkerSymbol?)
        map().layers().add(startMarkerLayer)
        map().layers().add(endMarkerLayer)
        logger.debug("Added marker layer")


        userPosLayer = ItemizedLayer(map(), null as MarkerSymbol?)
        map().layers().add(userPosLayer)
        logger.debug("Added user position layer")

        // Map position
        centerMap()
    }

    private fun centerMap() {
        val mapCenter = getCenterFromOsm(ghzExtractor.osmFilePath)
        map().setMapPosition(mapCenter.latitude, mapCenter.longitude, (1 shl 18).toDouble())
        logger.debug("Set map center to ${mapCenter.latitude}, ${mapCenter.longitude}")
    }

    private fun getCenterFromOsm(osmFilePath: String): GeoPoint {
        val readBoundsFromOsm: OsmBoundsExtractor.Bounds? = OsmBoundsExtractor.extractBoundsFromOsm(osmFilePath)

        if (readBoundsFromOsm != null) {

            val centerLat = (readBoundsFromOsm.minLat + readBoundsFromOsm.maxLat) / 2
            val centerLon = (readBoundsFromOsm.minLon + readBoundsFromOsm.maxLon) / 2

            return GeoPoint(centerLat, centerLon)
        }

        return GeoPoint(0, 0)
    }


    companion object {
        const val TAG = "de.ironjan.arionav.ionav.MapView"
    }


    interface MapEventsCallback {
        fun startPointChanged(coordinate: Coordinate)
        fun endPointChanged(coordinate: Coordinate)
        fun startPointCleared()
        fun endPointCleared()
        fun onRouteShown(pathWrapper: PathWrapper)
        fun onRouteCleared()
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
        logger.info("longpress at $p")

        if (notYetInitialized) {
            val msg = "Graph not loaded yet. Please wait."
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            logger.info(msg)
            return false
        }

        if (viewModel.hasBothCoordinates) {
            // clear start and end points
            clearRoute()
            viewModel.clearStartCoordinate()
            viewModel.clearEndCoordinate()
        }

        if (!viewModel.hasStartCoordinate) {
            viewModel.setStartCoordinate(Coordinate(p.latitude, p.longitude, selectedLevel))
            return true
        }

        if (!viewModel.hasEndCoordinate) {
            viewModel.setEndCoordinate(Coordinate(p.latitude, p.longitude, selectedLevel))

            computeAndShowRoute()
            return true
        }

        return true
    }


    private fun createMarkerItem(coordinate: Coordinate, resource: Int) = createMarkerItem(GeoPoint(coordinate.lat, coordinate.lon), resource)

    private fun createMarkerItem(p: GeoPoint?, resource: Int): MarkerItem {
        val drawable = resources.getDrawable(resource)
        val bitmap = AndroidGraphics.drawableToBitmap(drawable)
        val markerSymbol = MarkerSymbol(bitmap, 0.5f, 1f)
        val markerItem = MarkerItem("", "", p)
        markerItem.marker = markerSymbol
        return markerItem
    }

    private fun computeAndShowRoute() = showRoute(computeRoute())

    private fun showRoute(route: PathWrapper?) {
        if (route == null) {
            logger.info("Show route was called with a null route.")
            return
        }

        clearRoute()
        routeLayer = createRouteLayer(route)
        map().layers().add(routeLayer)
        redrawMap()
        mapEventsCallback.onRouteShown(route)
    }

    private fun clearRoute() {
        map().layers().remove(routeLayer)
        redrawMap()
        mapEventsCallback.onRouteCleared()
    }

    private fun createRouteLayer(route: PathWrapper): org.oscim.layers.vector.PathLayer {
        val style = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(-0x66ff33cd)
            .strokeWidth(4 * resources.displayMetrics.density)
            .build()
        val pathLayer = org.oscim.layers.vector.PathLayer(map(), style)
        val geoPoints = ArrayList<GeoPoint>()
        val pointList = route.points
        for (i in 0 until pointList.size)
            geoPoints.add(GeoPoint(pointList.getLatitude(i), pointList.getLongitude(i)))
        pathLayer.setPoints(geoPoints)
        return pathLayer
    }

    private fun computeRoute(): PathWrapper? = viewModel.computeRoute()

    private fun redrawMap() {
        map().updateMap(true)
    }

    fun centerOnUserPosition() {
        val lUserPosition = viewModel.getUserPositionLiveData().value ?: return

        val scale = map().mapPosition.scale
        map().setMapPosition(lUserPosition.lat, lUserPosition.lon, scale)

    }
}
