package de.ironjan.arionav_fw.ionav

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.MvvmCustomView
import de.ironjan.arionav_fw.ionav.mapview.*
import de.ironjan.arionav_fw.ionav.routing.RoutingService
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

    private var snackbar: Snackbar? = null

    // region map layers
    private lateinit var indoorLayers: IndoorLayersManager
    private lateinit var buildingLayer: BuildingLayer

    private lateinit var remainingRouteLayer: RouteLayer

    private lateinit var endMarkerLayer: DestinationMarkerLayer
    private lateinit var userPositionLayer: UserPositionLayer


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
        get() = if (::indoorLayers.isInitialized) indoorLayers.itemTapCallback else IndoorLayersManager.defaultTapCallback
        set(value) {
            indoorLayers.itemTapCallback = value
        }

    private fun observeLiveData(lifecycleOwner: LifecycleOwner) {

        endMarkerLayer.observe(viewModel, lifecycleOwner)

        userPositionLayer?.observe(viewModel, lifecycleOwner)

        viewModel.getMapCenterLiveData().observe(lifecycleOwner, Observer {
            if (viewModel.getFollowUserPositionLiveData().value == true
                && it != null
            ) {
                centerOn(it)
            }
        })


        remainingRouteLayer.observe(viewModel, lifecycleOwner)

        viewModel.selectedLevel.observe(lifecycleOwner, Observer {
            indoorLayers.selectedLevel = it.toDouble()
        })

        indoorLayers.observe(viewModel, lifecycleOwner)

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
        indoorLayers = IndoorLayersManager(map(), resources.displayMetrics.density)


        buildingLayer = BuildingLayer(map(), tileLayer)
        endMarkerLayer = DestinationMarkerLayer(map(), resources.getDrawable(R.drawable.marker_icon_red))
        userPositionLayer = UserPositionLayer(map())


        remainingRouteLayer = RouteLayer(map(), resources.displayMetrics.density)

        logger.debug("Created layers.")

        map().layers().add(buildingLayer)
        map().layers().add(endMarkerLayer)
        map().layers().add(remainingRouteLayer)
        logger.debug("Added layers.")
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
        val drawable: Drawable = resources.getDrawable(resource)
        val bitmap = AndroidGraphics.drawableToBitmap(drawable)
        val markerSymbol = MarkerSymbol(bitmap, 0.5f, 1f)

        val markerItem = MarkerItem(title, "description test", GeoPoint(coordinate.lat, coordinate.lon))
        markerItem.marker = markerSymbol


        return markerItem
    }


    fun redrawMap() = map().updateMap(true)


    fun centerOn(coordinate: Coordinate) {
        val scale = map().mapPosition.scale
        map().setMapPosition(coordinate.lat, coordinate.lon, scale)
        redrawMap()
    }


}
