package de.ironjan.arionav_fw.ionav.mapview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.IonavContainer
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.MvvmCustomViewModel
import de.ironjan.arionav_fw.ionav.navigation.NavigationService
import de.ironjan.arionav_fw.ionav.positioning.IPositionProvider
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.positioning.LevelDependentPositionProviderBaseImplementation
import de.ironjan.arionav_fw.ionav.positioning.PositioningService
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

class SimpleMapViewViewModel : ViewModel(), MvvmCustomViewModel<SimplifiedMapViewState> {
    private lateinit var routingService: RoutingService
    private lateinit var navigationService: NavigationService
    private lateinit var positioningService: PositioningService

    fun initialize(ionavContainer: IonavContainer){
        routingService = ionavContainer.routingService
        positioningService = ionavContainer.positioningService
        navigationService = ionavContainer.navigationService
        navigationService.registerObserver(object:NavigationService.RemainingRouteObserver{
            override fun update(remainingRoute: PathWrapper?) {
                    this@SimpleMapViewViewModel.remainingRoute.value = remainingRoute
            }
        })
    }

    val isRoutingInitialized: Boolean
        get() = ::routingService.isInitialized
                && routingService.initialized

    // FIXME should be IPositionObserver instead
    private var positionProvider: IPositionProvider ?= null


    override var state: SimplifiedMapViewState = SimplifiedMapViewState()





    private val endCoordinate: MutableLiveData<Coordinate?> = MutableLiveData()

    fun getEndCoordinateLifeData(): LiveData<Coordinate?> = endCoordinate

    fun setDestination(value: Coordinate?) {
        state.endCoordinate = value
        endCoordinate.value = value
        navigationService.destination = value
        logger.info("Updated destination to $value in view model.")
    }


    fun getUserPositionLiveData(): LiveData<IonavLocation?> = positioningService.lastKnownLocation


    private val currentRoute: MutableLiveData<PathWrapper?> = MutableLiveData()
    fun getCurrentRouteLiveData(): LiveData<PathWrapper?> {
        // FIXME better: cache route
        computeRoute()
        return currentRoute
    }


    private val remainingRoute: MutableLiveData<PathWrapper?> = MutableLiveData()
    fun getRemainingRouteLiveData(): LiveData<PathWrapper?> = remainingRoute


    private val canComputeRoute: Boolean
        get() = navigationService.initialized
                && state.endCoordinate != null

    private val followUserPosition: MutableLiveData<Boolean> = MutableLiveData(false)
    fun getFollowUserPositionLiveData(): LiveData<Boolean> = followUserPosition
    fun setFollowUserPosition(b : Boolean) {
        followUserPosition.value = b
    }

    private val showRemainingRoute: MutableLiveData<Boolean> = MutableLiveData(false)

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



    private fun computeRoute() {
        if (!canComputeRoute) {
            // just ignore. Called on every start and end coordinate change
            return
        }

        val lStartCoordinate = positioningService.lastKnownLocation.value?.coordinate
        val lEndCoordinate = state.endCoordinate
        if (lStartCoordinate == null) {
            logger.info("computeRoute was called with null for start coordinate.")
            return
        }
        if (lEndCoordinate == null) {
            logger.info("computeRoute was called with null for end coordinate.")
            return
        }

        currentRoute.value = routingService.route(lStartCoordinate, lEndCoordinate)
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