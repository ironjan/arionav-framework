package de.ironjan.arionav.ionav

import androidx.lifecycle.ViewModel
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import de.ironjan.graphhopper.extensions_core.Coordinate
import de.ironjan.graphhopper.levelextension.Routing
import org.slf4j.LoggerFactory

class MapViewViewModel(var hopper: GraphHopper? = null) : ViewModel() {
    var startCoordinate: Coordinate? = null
    var endCoordinate: Coordinate? = null
    var currentRoute: PathWrapper? = null

    val hasStartCoordinate: Boolean
      get() = startCoordinate != null

    val hasEndCoordinate: Boolean
      get() = endCoordinate != null

    val hasBothCoordinates: Boolean
    get() = hasStartCoordinate && hasEndCoordinate

    fun clearStartCoordinate() { startCoordinate = null }
    fun clearEndCoordinate() { endCoordinate = null }

    var currentUserPosition: Coordinate? = null




    val canComputeRoute: Boolean
    get() = hopper != null
            && startCoordinate != null
            && endCoordinate != null



    private val logger = LoggerFactory.getLogger("MapViewViewModel")

    fun computeRoute(): PathWrapper? {
        if(!canComputeRoute) {
            // TODO throw exception...
            return null
        }

        val lStartCoordinate = startCoordinate
        val lEndCoordinate = endCoordinate
        if (lStartCoordinate == null || lEndCoordinate == null) {
            logger.info("computeRoute was called with null for either start coordinate or end coordinate (start: $lStartCoordinate, end: $lEndCoordinate).")
            return null
        }

        return try {
            val route = Routing(hopper).route(lStartCoordinate, lEndCoordinate)
            logger.debug("Computed route: $route")
            route
        } catch (e: java.lang.Exception) {
            e.printStackTrace()

            null
        }

    }
}