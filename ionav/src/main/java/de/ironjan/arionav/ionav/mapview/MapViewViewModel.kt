package de.ironjan.arionav.ionav.mapview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import de.ironjan.arionav.ionav.custom_view_mvvm.MvvmCustomViewModel
import de.ironjan.arionav.ionav.positioning.IPositionObserver
import de.ironjan.arionav.ionav.positioning.PositionListenerBaseImplementation
import de.ironjan.graphhopper.extensions_core.Coordinate
import de.ironjan.graphhopper.levelextension.Routing
import org.slf4j.LoggerFactory
import java.nio.file.Path

class MapViewViewModel(var hopper: GraphHopper? = null) : ViewModel(), MvvmCustomViewModel<MapViewState> {
    override var state: MapViewState = MapViewState()

    private val startCoordinate: MutableLiveData<Coordinate?> = MutableLiveData()

    fun getStartCoordinateLifeData(): LiveData<Coordinate?> = startCoordinate
    fun setStartCoordinate(value: Coordinate?) {
        state.startCoordinate = value
        startCoordinate.value = value
        logger.info("Updated start coordinate to $value in view model.")
    }

    fun clearStartCoordinate() = setStartCoordinate(null)

    private val endCoordinate: MutableLiveData<Coordinate?> = MutableLiveData()

    fun getEndCoordinateLifeData(): LiveData<Coordinate?> = endCoordinate

    fun setEndCoordinate(value: Coordinate?) {
        state.endCoordinate = value
        endCoordinate.value = value
        logger.info("Updated end coordinate to $value in view model.")
        computeRoute()
    }
    fun clearEndCoordinate() = setEndCoordinate(null)

    private val userPosition: MutableLiveData<Coordinate?> = MutableLiveData()
    fun getUserPositionLiveData(): LiveData<Coordinate?> = userPosition
    
    

    private val currentRoute: MutableLiveData<PathWrapper?> = MutableLiveData()
    fun getCurrentRouteLiveData(): LiveData<PathWrapper?> = currentRoute

    val hasStartCoordinate: Boolean
      get() = state.startCoordinate != null

    val hasEndCoordinate: Boolean
      get() = state.endCoordinate != null

    val hasBothCoordinates: Boolean
    get() = hasStartCoordinate && hasEndCoordinate






    val canComputeRoute: Boolean
    get() = hopper != null
            && state.startCoordinate != null
            && state.endCoordinate  != null



    private val logger = LoggerFactory.getLogger("MapViewViewModel")

    fun computeRoute(): PathWrapper? {
        if(!canComputeRoute) {
            // TODO throw exception...
            return null
        }

        val lStartCoordinate = state.startCoordinate
        val lEndCoordinate = state.endCoordinate
        if (lStartCoordinate == null || lEndCoordinate == null) {
            logger.info("computeRoute was called with null for either start coordinate or end coordinate (start: $lStartCoordinate, end: $lEndCoordinate).")
            return null
        }

        return try {
            val route = Routing(hopper).route(lStartCoordinate, lEndCoordinate)
            logger.debug("Computed route: $route")
            currentRoute.value = route
            route
        } catch (e: java.lang.Exception) {
            e.printStackTrace()

            null
        }

    }

    private val iPositionObserver = object : IPositionObserver {
        override fun onPositionChange(c: Coordinate?) {
            userPosition.value = c
        }
    }

    fun setUserPositionProvider(positionProvider: PositionListenerBaseImplementation) {
        positionProvider.registerObserver(iPositionObserver)
    }
}