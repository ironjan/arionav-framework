package de.ironjan.arionav_fw.ionav.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.MvvmCustomView
import de.ironjan.arionav_fw.ionav.services.RoutingService
import de.ironjan.arionav_fw.ionav.views.mapview.*
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.oscim.android.MapView
import org.oscim.core.GeoPoint
import org.oscim.event.Gesture
import org.oscim.event.GestureListener
import org.oscim.event.MotionEvent
import org.oscim.layers.Layer
import org.oscim.layers.tile.buildings.BuildingLayer
import org.oscim.layers.tile.vector.VectorTileLayer
import org.oscim.theme.VtmThemes
import org.oscim.tiling.source.mapfile.MapFileTileSource
import org.slf4j.LoggerFactory

class IonavMapView : MapView, MvvmCustomView<IonavViewModel> {

    private val logger = LoggerFactory.getLogger(IonavMapView::class.simpleName)

    // region map layers
    private lateinit var indoorLayers: IndoorLayersManager
    private lateinit var buildingLayer: BuildingLayer
    private lateinit var remainingRouteLayer: RouteLayer
    private lateinit var endMarkerLayer: DestinationMarkerLayer
    private lateinit var userPositionLayer: UserPositionLayer
    // endregion

    // region MVVM
    override lateinit var viewModel: IonavViewModel

    private lateinit var lifecycleOwner: LifecycleOwner
    override fun onLifecycleOwnerAttached(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
    }
    // endregion

    // region constructors
    constructor(context: Context, attrsSet: AttributeSet) : super(context, attrsSet)

    constructor(context: Context) : super(context, null)
    // endregion


    // region initialization
    fun initialize(viewModel: IonavViewModel) {
        this.viewModel = viewModel

        val tileLayer = loadMap(viewModel.mapFilePath)
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

    private fun goToMapStartPosition() {
        // Map start position
        val mapCenter = GeoPoint(51.731938, 8.734518)
        val zoom = (1 shl 19).toDouble()
        map().setMapPosition(mapCenter.latitude, mapCenter.longitude, zoom)
        logger.debug("Set map center to ${mapCenter.latitude}, ${mapCenter.longitude} with $zoom")
    }

    private fun observeLiveData(lifecycleOwner: LifecycleOwner) {

        endMarkerLayer.observe(viewModel, lifecycleOwner)
        userPositionLayer.observe(viewModel, lifecycleOwner)
        remainingRouteLayer.observe(viewModel, lifecycleOwner)
        indoorLayers.observe(viewModel, lifecycleOwner)

        viewModel.mapCenter.observe(lifecycleOwner, Observer {
            if (viewModel.isFollowUser
                && it != null
            ) {
                centerOn(it)
            }
        })
    }
    // endregion



    // region indoor layer callback
    var itemTapCallback: IndoorItemTapCallback
        get() = if (::indoorLayers.isInitialized) indoorLayers.itemTapCallback else IndoorLayersManager.defaultTapCallback
        set(value) {
            indoorLayers.itemTapCallback = value
        }
    // endregion

    // region map long presses
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

            viewModel.setDestination(Coordinate(p.latitude, p.longitude, selectedLevel))
            return true
        }

        val msg = "Graph not loaded yet. Please wait."
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        logger.info(msg)
        return false
    }
    // endregion

    // region map helpers

    fun redrawMap() = map().updateMap(true)


    fun centerOn(coordinate: Coordinate) {
        val scale = map().mapPosition.scale
        map().setMapPosition(coordinate.lat, coordinate.lon, scale)
        redrawMap()
    }

    fun centerOnUser() {
        val coordinate = viewModel.userLocation.value ?: return
        centerOn(coordinate)
    }

    // endregion

}