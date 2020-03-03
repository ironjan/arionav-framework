package de.ironjan.arionav.ionav.mapview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import de.ironjan.arionav.ionav.custom_view_mvvm.MvvmCustomViewModel
import de.ironjan.arionav.ionav.positioning.IPositionProvider
import de.ironjan.arionav.ionav.positioning.IonavLocation
import de.ironjan.arionav.ionav.positioning.LevelDependentPositionProviderBaseImplementation
import de.ironjan.arionav.ionav.positioning.PositioningProviderRegistry
import de.ironjan.graphhopper.extensions_core.Coordinate
import de.ironjan.graphhopper.levelextension.Routing
import org.slf4j.LoggerFactory

class MapViewViewModel(var hopper: GraphHopper? = null) : ViewModel(), MvvmCustomViewModel<MapViewState> {
    // FIXME should be IPositionObserver instead
    private var positionProvider: IPositionProvider ?= null


    override var state: MapViewState = MapViewState()

    private val startCoordinate: MutableLiveData<Coordinate?> = MutableLiveData()

    fun getStartCoordinateLifeData(): LiveData<Coordinate?> = startCoordinate
    fun setStartCoordinate(value: Coordinate?) {
        state.startCoordinate = value
        startCoordinate.value = value
        logger.info("Updated start coordinate to $value in view model.")
        computeRoute()
    }

    fun clearStartAndEndCoordinates() {
        clearStartCoordinate()
        clearEndCoordinate()
        currentRoute.value = null
        remainingRoute.value = null
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

    fun getUserPositionLiveData(): LiveData<IonavLocation?> = PositioningProviderRegistry.Instance.lastKnownLocation


    private val currentRoute: MutableLiveData<PathWrapper?> = MutableLiveData()
    fun getCurrentRouteLiveData(): LiveData<PathWrapper?> {
        // FIXME better: cache route
        computeRoute()
        return currentRoute
    }
    private val hasRoute: Boolean
        get() = currentRoute.value != null
    val hasStartCoordinate: Boolean
        get() = state.startCoordinate != null

    val hasEndCoordinate: Boolean
        get() = state.endCoordinate != null

    val hasBothCoordinates: Boolean
        get() = hasStartCoordinate && hasEndCoordinate

    private val remainingRoute: MutableLiveData<PathWrapper?> = MutableLiveData()
    fun getRemainingRouteLiveData(): LiveData<PathWrapper?> {
        // FIXME better: cache route
        return remainingRoute
    }

    private val canComputeRoute: Boolean
        get() = hopper != null
                && state.startCoordinate != null
                && state.endCoordinate != null

    private val followUserPosition: MutableLiveData<Boolean> = MutableLiveData(false)
    fun getFollowUserPositionLiveData(): LiveData<Boolean> = followUserPosition
    fun toggleFollowUserPosition() {
        val value = followUserPosition.value?: false
        val not = value.not()
        followUserPosition.value = not
    }
    fun setFollowUserPosition(b : Boolean) {
        followUserPosition.value = b
    }

    private val showRemainingRoute: MutableLiveData<Boolean> = MutableLiveData(false)
    fun toggleShowRemainingRoute() {
        val value1 = showRemainingRoute.value
        val not = value1?.not()
        val value = not ?: false
        showRemainingRoute.value = value
        if(value) recomputeRemainingRoute()
    }

    fun getShowRemainingRouteLiveData(): LiveData<Boolean> = showRemainingRoute
    fun getShowRemainingRouteCurrentValue(): Boolean = showRemainingRoute.value ?: false

    private val  levelList = MutableLiveData(listOf(-1.0, 0.0, 1.0, 2.0))
    val initalLevelListPosition = 1
    private val selectedLevelListPosition = MutableLiveData(initalLevelListPosition)

    fun getLevelListLiveData(): LiveData<List<Double>> = levelList
    fun getSelectedLevelListPosition(): LiveData<Int> = selectedLevelListPosition
    fun selectLevelListPosition(pos: Int) {
        selectedLevelListPosition.value = pos
        val lLevelList = levelList.value ?: return

        val lPositionProvider = positionProvider
        // todo remove this workaround
        if(lPositionProvider is LevelDependentPositionProviderBaseImplementation){
            lPositionProvider.currentLevel = lLevelList[pos]
        }
    }
    fun getSelectedLevel(): Double {
        return levelList.value?.get(selectedLevelListPosition.value ?: initalLevelListPosition) ?: 0.0
    }

    private val logger = LoggerFactory.getLogger("MapViewViewModel")



    internal fun computeRoute() {
        if (!canComputeRoute) {
            // just ignore. Called on every start and end coordinate change
            return
        }

        val lStartCoordinate = state.startCoordinate
        val lEndCoordinate = state.endCoordinate
        if (lStartCoordinate == null || lEndCoordinate == null) {
            logger.info("computeRoute was called with null for either start coordinate or end coordinate (start: $lStartCoordinate, end: $lEndCoordinate).")
            return
        }

        currentRoute.value = computeRouteFromTo(lStartCoordinate, lEndCoordinate)
    }

    private fun recomputeRemainingRoute() {
        val lUserPosition = getUserPositionLiveData().value
        val lEndCoordinate = state.endCoordinate
        if (lUserPosition == null || lEndCoordinate == null) {
            logger.info("recomputeRemainingRoute was called with null for either start coordinate or end coordinate (start: $lEndCoordinate, end: $lUserPosition).")
            return
        }

        val computedRoute = computeRouteFromTo(lUserPosition, lEndCoordinate)
        remainingRoute.value = computedRoute
    }

    private fun computeRouteFromTo(lStartCoordinate: Coordinate, lEndCoordinate: Coordinate): PathWrapper? {
        return try {
            Routing(hopper).route(lStartCoordinate, lEndCoordinate)
        } catch (e: Exception) {
            e.printStackTrace()

            null
        }
    }


    fun setStartCoordinateToUserPos() {
        setStartCoordinate(getUserPositionLiveData().value?: return)
    }



    private val mapCenter: MutableLiveData<Coordinate?> = MutableLiveData(null)
    fun getMapCenterLiveData(): LiveData<Coordinate?> = mapCenter
    fun setMapCenter(c: IonavLocation) {
        mapCenter.value = c
    }

    fun centerOnUserPos() {
        setMapCenter(getUserPositionLiveData().value ?: return)
    }
}