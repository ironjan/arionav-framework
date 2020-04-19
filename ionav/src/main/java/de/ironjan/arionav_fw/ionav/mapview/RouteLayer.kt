package de.ironjan.arionav_fw.ionav.mapview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenMapLayer
import org.oscim.backend.canvas.Color
import org.oscim.core.GeoPoint
import org.oscim.layers.vector.PathLayer
import org.oscim.layers.vector.geometries.Style
import org.oscim.map.Map
import org.slf4j.LoggerFactory


class RouteLayer(private val map: Map, style: Style):
    PathLayer(map, style),
    ModelDrivenMapLayer<SimplifiedMapViewState, SimpleMapViewViewModel> {
    private val logger = LoggerFactory.getLogger(RouteLayer::class.simpleName)

    override fun observe(viewModel: SimpleMapViewViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.getRemainingRouteLiveData().observe(lifecycleOwner, Observer {
            route = it
        })
    }
    var route: PathWrapper? = null
        set(value) {
            field = value
            redrawLayer()
        }

    private fun redrawLayer() {
        clearPath()
        map.updateMap(true)
        logger.debug("Cleared the displayed route.")


        val route = route

        if (route == null) {
            logger.debug("show remaining route was called with null route.")
            return
        }

        if (route.hasErrors()) {
            val errorString = route.errors.map { it.message }.joinToString(", ")
            logger.warn("Route $route has errors and cannot be shown: $errorString")
            // FIXME show error
            return
        }

        val points = route.points.map { GeoPoint(it.lat, it.lon) }
        setPoints(points)
        map.updateMap(true)
        logger.warn("Updated displayed route to $points")
    }

    constructor(map: Map, density: Float, color: Int = Color.GREEN): this(map, defaultStyle(density, color))

    companion object {
        fun defaultStyle(density: Float, color: Int): Style =  Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(color)
            .strokeWidth(4 * density)
            .build()
    }
}