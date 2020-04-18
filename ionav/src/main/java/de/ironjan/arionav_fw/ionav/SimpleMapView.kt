package de.ironjan.arionav_fw.ionav

import android.content.Context
import android.os.AsyncTask
import android.util.AttributeSet
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.MvvmCustomView
import de.ironjan.arionav_fw.ionav.mapview.*
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import de.ironjan.arionav_fw.ionav.routing.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.routing.model.readers.IndoorMapDataLoadingTask
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
import org.oscim.layers.tile.vector.VectorTileLayer
import org.oscim.theme.VtmThemes
import org.oscim.tiling.source.mapfile.MapFileTileSource
import org.slf4j.LoggerFactory

class SimpleMapView : MapView, MvvmCustomView<SimplifiedMapViewState, SimpleMapViewViewModel> {

    private  var snackbar: Snackbar? = null

    // region map layers
    private lateinit var indoorLayers: IndoorLayers
    private lateinit var buildingLayer: BuildingLayer

    private lateinit var remainingRouteLayer: RouteLayer

    private var endMarkerLayer: ItemizedLayer<MarkerItem>? = null
    private var userPositionLayer: UserPositionLayer? = null


    private val endCoordinateMarker = R.drawable.marker_icon_red
    // endregion

    // region MVVM
    override val viewModel = SimpleMapViewViewModel()
    private lateinit var lifecycleOwner: LifecycleOwner

    override fun onLifecycleOwnerAttached(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
    }
    // endregion

    var itemTapCallback: IndoorItemTapCallback
        get() = if (::indoorLayers.isInitialized) indoorLayers.itemTapCallback else IndoorLayers.defaultTapCallback
        set(value) {
            indoorLayers.itemTapCallback = value
        }

    private fun observeLiveData(lifecycleOwner: LifecycleOwner) {

        viewModel.getEndCoordinateLifeData().observe(lifecycleOwner, Observer {
            updateMarkerLayer(endMarkerLayer, it, endCoordinateMarker, "destination")

            logger.debug("Updated end coordinate in view to $it.")
        })

        userPositionLayer?.observe(viewModel, lifecycleOwner)

        viewModel.getMapCenterLiveData().observe(lifecycleOwner, Observer {
            if (viewModel.getFollowUserPositionLiveData().value == true
                && it != null
            ) {
                centerOn(it)
            }
        })

        viewModel.getFollowUserPositionLiveData().observe(lifecycleOwner, Observer {
            if (!it) return@Observer

            viewModel.centerOnUserPos()
        })


        viewModel.getRemainingRouteLiveData().observe(lifecycleOwner, Observer {
            showRemainingRoute(it)
        })

        viewModel.selectedLevel.observe(lifecycleOwner, Observer {
            indoorLayers.selectedLevel = it.toDouble()
        })

    }

    private fun updateMarkerLayer(
        layer: ItemizedLayer<MarkerItem>?,
        it: Coordinate?,
        marker: Int,
        title: String = ""
    ) {
        layer?.removeAllItems()
        redrawMap()

        if (it == null) return

        layer?.addItem(createMarkerItem(it, marker, title))
        redrawMap()
    }

    constructor(context: Context, attrsSet: AttributeSet) : super(context, attrsSet) {}

    constructor(context: Context) : super(context, null) {}


    private val logger = LoggerFactory.getLogger(TAG)

    fun initialize(ionavContainer: IonavContainer) {
        viewModel.initialize(ionavContainer)

        val tileLayer = loadMap(ionavContainer.mapFilePath)
        createAndAddLayers(tileLayer)

        goToMapStartPosition()

        loadAndShowIndoorData(ionavContainer.osmFilePath)

        observeLiveData(lifecycleOwner)
    }


    private fun loadMap(osmFilePath: String): VectorTileLayer {
        logger.debug("Loading map for map view")
        map().layers().add(MapEventsReceiver(map()))

        val tileSource = MapFileTileSource()

        tileSource.setMapFile(osmFilePath)
        logger.debug("Set tile source to $osmFilePath")

        val tileLayer = map().setBaseMap(tileSource)
        map().setTheme(VtmThemes.DEFAULT)
        return tileLayer
    }

    private fun goToMapStartPosition() {
        // Map start position
        val mapCenter = GeoPoint(51.731938, 8.734518)
        val zoom = (1 shl 19).toDouble()
        map().setMapPosition(mapCenter.latitude, mapCenter.longitude, zoom)
        logger.debug("Set map center to ${mapCenter.latitude}, ${mapCenter.longitude} with $zoom")
    }

    private fun createAndAddLayers(tileLayer: VectorTileLayer) {
        indoorLayers = IndoorLayers(map(), resources.displayMetrics.density)


        buildingLayer = BuildingLayer(map(), tileLayer)
        endMarkerLayer = ItemizedLayer(map(), null as MarkerSymbol?)
        userPositionLayer = UserPositionLayer(map())


        remainingRouteLayer = RouteLayer(map(), resources.displayMetrics.density)

        logger.debug("Created layers.")

        map().layers().add(buildingLayer)
        map().layers().add(endMarkerLayer)
        map().layers().add(remainingRouteLayer)
        logger.debug("Added layers.")
    }

    private fun loadAndShowIndoorData(osmFilePath: String) {
        val callback = object : IndoorMapDataLoadingTask.OnIndoorMapDataLoaded {
            override fun loadCompleted(indoorData: IndoorData) {
                showIndoorMapData(indoorData)
            }

        }

        IndoorMapDataLoadingTask(osmFilePath, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        logger.info("Started loading of indoor map data.")
    }

    private fun showIndoorMapData(indoorData: IndoorData) {
        logger.info("Completed loading of indoor map.")

        indoorLayers.indoorData = indoorData
        indoorLayers.selectedLevel = (viewModel.selectedLevel.value ?: 0).toDouble()
        map().updateMap()
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
        const val TAG = "IonavMapView" // FIXME rename class
    }


    internal inner class MapEventsReceiver(map: org.oscim.map.Map) : Layer(map), GestureListener {

        override fun onGesture(g: Gesture, e: MotionEvent): Boolean {
            if (g is Gesture.LongPress) {
                val p = mMap.viewport().fromScreenPoint(e.x, e.y)
                return onLongPress(p)
            }

            logger.debug("Gesture: $g, MotionEvent: ${e.action}, ${e.x}, ${e.y}, count: ${e.pointerCount}, time: ${e.time}")
            viewModel.setFollowUserPosition(false)
            return false
        }

        override fun onDetach() {
            super.onDetach()
            logger.debug("ondetach")
        }

    }


    private fun onLongPress(p: GeoPoint): Boolean {
        logger.info("longpress at $p")

        if (viewModel.routingStatus.value == RoutingService.Status.READY) {
            val selectedLevel = viewModel.getSelectedLevel()

            viewModel.setDestination(Coordinate(p.latitude, p.longitude, selectedLevel.toDouble()))
            return true
        }

        val msg = "Graph not loaded yet. Please wait."
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        logger.info(msg)
        return false
    }


    private fun createMarkerItem(coordinate: Coordinate, resource: Int, title: String = ""): MarkerItem {
        val drawable = resources.getDrawable(resource)
        val bitmap = AndroidGraphics.drawableToBitmap(drawable)
        val markerSymbol = MarkerSymbol(bitmap, 0.5f, 1f)

        val markerItem = MarkerItem(title, "description test", GeoPoint(coordinate.lat, coordinate.lon))
        markerItem.marker = markerSymbol


        return markerItem
    }


    private fun showRemainingRoute(remainingRoute: PathWrapper?) {
        remainingRouteLayer.clearPath()
        redrawMap()
        logger.debug("Cleared the displayed route.")


        if (remainingRoute == null) {
            logger.debug("show remaining route was called with null route.")
            return
        }

        if (remainingRoute.hasErrors()) {
            val errorString = remainingRoute.errors.map { it.message }.joinToString(", ")
            logger.warn("Route $remainingRoute has errors and cannot be shown: $errorString")
            Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show()
            return
        }

        val points = remainingRoute.points.map { GeoPoint(it.lat, it.lon) }
        remainingRouteLayer.setPoints(points)
        redrawMap()
        logger.warn("Updated displayed route to $points")
    }

    fun redrawMap() = map().updateMap(true)


    fun centerOn(coordinate: Coordinate) {
        val scale = map().mapPosition.scale
        map().setMapPosition(coordinate.lat, coordinate.lon, scale)
        redrawMap()
    }


}
