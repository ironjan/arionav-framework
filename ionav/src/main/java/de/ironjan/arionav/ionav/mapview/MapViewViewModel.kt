package de.ironjan.arionav.ionav.mapview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import com.graphhopper.util.Instruction
import de.ironjan.arionav.ionav.custom_view_mvvm.MvvmCustomViewModel
import de.ironjan.arionav.ionav.positioning.IPositionObserver
import de.ironjan.arionav.ionav.positioning.LevelDependentPositionProviderBaseImplementation
import de.ironjan.arionav.ionav.positioning.PositionProviderBaseImplementation
import de.ironjan.graphhopper.extensions_core.Coordinate
import de.ironjan.graphhopper.levelextension.Routing
import org.slf4j.LoggerFactory
import java.lang.Exception

class MapViewViewModel(var hopper: GraphHopper? = null) : ViewModel(), MvvmCustomViewModel<MapViewState> {
    // FIXME should be IPositionObserver instead
    private var positionProvider: LevelDependentPositionProviderBaseImplementation?= null

    private val nextInstruction: MutableLiveData<Instruction?> = MutableLiveData()
    fun getNextInstructionLiveData(): LiveData<Instruction?> = nextInstruction

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

    private val userPosition: MutableLiveData<Coordinate?> = MutableLiveData()
    fun getUserPositionLiveData(): LiveData<Coordinate?> = userPosition


    private val currentRoute: MutableLiveData<PathWrapper?> = MutableLiveData()
    fun getCurrentRouteLiveData(): LiveData<PathWrapper?> = currentRoute
    private val hasRoute: Boolean
        get() = currentRoute.value != null
    val hasStartCoordinate: Boolean
        get() = state.startCoordinate != null

    val hasEndCoordinate: Boolean
        get() = state.endCoordinate != null

    val hasBothCoordinates: Boolean
        get() = hasStartCoordinate && hasEndCoordinate

    private val remainingRoute: MutableLiveData<PathWrapper?> = MutableLiveData()
    fun getRemainingRouteLiveData(): LiveData<PathWrapper?> = remainingRoute

    private val canComputeRoute: Boolean
        get() = hopper != null
                && state.startCoordinate != null
                && state.endCoordinate != null

    private val followUserPosition: MutableLiveData<Boolean> = MutableLiveData(false)
    fun getFollowUserPositionLiveData(): LiveData<Boolean> = followUserPosition
    fun toggleFollowUserPosition() {
        followUserPosition.value = followUserPosition.value?.not()
    }
    fun setFollowUserPosition(b : Boolean) {
        followUserPosition.value = b
    }

    private val showRemainingRoute: MutableLiveData<Boolean> = MutableLiveData(false)
    fun toggleShowRemainingRoute() {
        showRemainingRoute.value = showRemainingRoute.value?.not()
    }

    fun getShowRemainingRouteLiveData(): LiveData<Boolean> = showRemainingRoute
    fun getShowRemainingRouteCurrentValue(): Boolean = showRemainingRoute.value ?: false

    private val  levelList = MutableLiveData(listOf(-2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0))
    private val selectedLevelListPosition = MutableLiveData(4)

    fun getLevelListLiveData(): LiveData<List<Double>> = levelList
    fun getSelectedLevelListPosition(): LiveData<Int> = selectedLevelListPosition
    fun selectLevelListPosition(pos: Int) {
        selectedLevelListPosition.value = pos
        val lLevelList = levelList.value ?: return
        positionProvider?.currentLevel = lLevelList[pos]
    }
    fun getSelectedLevel(): LiveData<Double> = MutableLiveData(levelList.value?.get(selectedLevelListPosition.value?:4))

    private val logger = LoggerFactory.getLogger("MapViewViewModel")


    private val iPositionObserver = object : IPositionObserver {
        override fun onPositionChange(c: Coordinate?) {
            userPosition.value = c
            if (hasRoute) {
                recomputeRemainingRoute()
            }
        }
    }


    private fun computeRoute() {
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
        val lUserPosition = userPosition.value
        val lEndCoordinate = state.endCoordinate
        if (lUserPosition == null || lEndCoordinate == null) {
            logger.info("recomputeRemainingRoute was called with null for either start coordinate or end coordinate (start: $lEndCoordinate, end: $lUserPosition).")
            return
        }

        val computedRoute = computeRouteFromTo(lUserPosition, lEndCoordinate)
        remainingRoute.value = computedRoute
        nextInstruction.value =computedRoute?.instructions?.first()
    }

    private fun computeRouteFromTo(lStartCoordinate: Coordinate, lEndCoordinate: Coordinate): PathWrapper? {
        return try {
            Routing(hopper).route(lStartCoordinate, lEndCoordinate)
        } catch (e: Exception) {
            e.printStackTrace()

            null
        }
    }

    fun setUserPositionProvider(positionProvider: LevelDependentPositionProviderBaseImplementation) {
        positionProvider.registerObserver(iPositionObserver)
        this.positionProvider = positionProvider
    }
}