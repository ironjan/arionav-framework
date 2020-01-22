package de.ironjan.arionav.ionav

import android.content.Context
import android.util.AttributeSet
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import de.ironjan.graphhopper.extensions_core.Coordinate
import de.ironjan.graphhopper.levelextension.Routing
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

class MapView: MapView {
    constructor(context: Context, attrsSet: AttributeSet) : super(context, attrsSet) {}

    constructor(context: Context): super(context, null){}

    private var userPosition: Coordinate? = null
    private lateinit var mapEventsCallback: MapEventsCallback
    private lateinit var ghzExtractor: GhzExtractor
    private var endCoordinate: Coordinate? = null
    private var startCoordinate: Coordinate? = null
    private var routeLayer: org.oscim.layers.vector.PathLayer? = null
    var selectedLevel: Double = 0.0

    private var hopper: GraphHopper? = null

    private var startEndMarkerLayer: ItemizedLayer<MarkerItem>? = null
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
                hopper = graphHopper
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

        startEndMarkerLayer = ItemizedLayer(map(), null as MarkerSymbol?)
        map().layers().add(startEndMarkerLayer)
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
        var readBoundsFromOsm: OsmBoundsExtractor.Bounds? = OsmBoundsExtractor.extractBoundsFromOsm(osmFilePath)

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



    interface MapEventsCallback{
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
        logger.debug("longpress at $p")
        if (hopper == null) {
            // FIXME show message
            logger.info("Graph not loaded yet. Ignoring long tap.")
            return false
        }

        if (startCoordinate != null && endCoordinate != null) {
            // clear start and end points
            clearRoute()
            startEndMarkerLayer?.removeAllItems()
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
        if(p == null){
            startCoordinate = null
            mapEventsCallback.startPointCleared()
            return
        }


        startEndMarkerLayer?.addItem(createMarkerItem(p, R.drawable.marker_icon_green))
        map().updateMap(true)

        val coordinate = Coordinate(p.latitude, p.longitude, selectedLevel)
        startCoordinate = coordinate
        mapEventsCallback.startPointChanged(coordinate)
    }


    private fun setEndCoordnate(p: GeoPoint?) {
        if(p == null){
            endCoordinate = null
            mapEventsCallback.endPointCleared()
            return
        }

        startEndMarkerLayer?.addItem(createMarkerItem(p, R.drawable.marker_icon_red))
        map().updateMap(true)

        val coordinate = Coordinate(p.latitude, p.longitude, selectedLevel)
        endCoordinate = coordinate
        mapEventsCallback.endPointChanged(coordinate)
    }

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
        map().updateMap(true)
        mapEventsCallback.onRouteShown(route)
    }

    private fun clearRoute() {
        map().layers().remove(routeLayer)
        map().updateMap(true)
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

    private fun computeRoute(): PathWrapper? {
        val lStartCoordinate = startCoordinate
        val lEndCoordinate = endCoordinate
        if (lStartCoordinate == null || lEndCoordinate == null) {
            logger.info("computeRoute was called with null for either start coordinate or end coordinate (start: $lStartCoordinate, end: $lEndCoordinate).")
            return null
        }

        try {
            val route = Routing(hopper).route(lStartCoordinate, lEndCoordinate)
            logger.debug("Computed route: $route")
            return route
        } catch (e: java.lang.Exception) {
            e.printStackTrace()

            return null
        }

    }

    fun setUserPosition(coordinate: Coordinate) {
        userPosition = coordinate

        updateUserPositionOnMap()
    }

    private fun updateUserPositionOnMap() {
        val lUserPosition = userPosition ?: return
        userPosLayer?.removeAllItems()
        userPosLayer?.addItem(createMarkerItem(GeoPoint(lUserPosition.lat, lUserPosition.lon), R.drawable.marker_icon_blue))
        map().updateMap(true)
    }

    fun centerOnUserPosition() {
        val lUserPosition = userPosition ?: return

        val scale = map().mapPosition.scale
        map().setMapPosition(lUserPosition.lat, lUserPosition.lon, scale)

    }
}
