package de.ironjan.arionav_fw.ionav.views.mapview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenMapExtension
import org.oscim.backend.canvas.Color
import org.oscim.core.GeoPoint
import org.oscim.layers.vector.PathLayer
import org.oscim.layers.vector.geometries.Style
import org.oscim.map.Map
import org.slf4j.LoggerFactory


class RouteLayer(private val map: Map, style: Style) : PathLayer(map, style),
    ModelDrivenMapExtension<IonavViewModel> {
    private val logger = LoggerFactory.getLogger(RouteLayer::class.simpleName)

    override fun observe(viewModel: IonavViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.route.observe(lifecycleOwner, Observer {
            redrawLayer(it)
        })
    }

    private fun redrawLayer(route: PathWrapper?) {
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

    constructor(map: Map, density: Float, color: Int = Color.GREEN) : this(map, defaultStyle(density, color))

    companion object {
        fun defaultStyle(density: Float, color: Int): Style = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(color)
            .strokeWidth(4 * density)
            .build()
    }
}