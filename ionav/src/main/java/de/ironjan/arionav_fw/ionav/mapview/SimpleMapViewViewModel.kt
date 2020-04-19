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
        navigationService.registerObserver(object:NavigationService.NavigationServiceObserver{
            override fun update(value: Coordinate?) {
                state.endCoordinate = value
                endCoordinate.value = value
                logger.info("Updated destination to $value in view model.")
            }

            override fun update(remainingRoute: PathWrapper?) {
                    this@SimpleMapViewViewModel.remainingRoute.value = remainingRoute
            }
        })

        routingService.registerObserver(object : RoutingService.RoutingServiceStatusObserver {
            override fun update(v: RoutingService.Status) {
                _routingStatus.value = v
            }
        })
    }

    private val _routingStatus: MutableLiveData<RoutingService.Status> = MutableLiveData(RoutingService.Status.UNINITIALIZED)
    val routingStatus: LiveData<RoutingService.Status> = _routingStatus

    // FIXME should be IPositionObserver instead
    private var positionProvider: IPositionProvider ?= null


    override var state: SimplifiedMapViewState = SimplifiedMapViewState()





    private val endCoordinate: MutableLiveData<Coordinate?> = MutableLiveData()

    fun getEndCoordinateLifeData(): LiveData<Coordinate?> = endCoordinate

    fun setDestination(value: Coordinate?) {
        navigationService.destination = value
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

        if(b) centerOnUserPos()
    }

    private val _selectedLevel = MutableLiveData(0)
    val selectedLevel: LiveData<Int> = _selectedLevel

    fun getSelectedLevel(): Int {
        return 0
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

    fun increaseLevel() {
        val oldValue = _selectedLevel.value ?: 0
        val newValue = oldValue + 1
        _selectedLevel.value = newValue
    }

    fun decreaseLevel() {
        val oldValue = _selectedLevel.value ?: 0
        val newValue = oldValue - 1
        _selectedLevel.value = newValue
    }
}