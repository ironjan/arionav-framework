package de.ironjan.arionav_fw.ionav.mapview

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.IonavContainer
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.MvvmCustomViewModel
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.model.readers.IndoorMapDataLoadingTask
import de.ironjan.arionav_fw.ionav.navigation.NavigationService
import de.ironjan.arionav_fw.ionav.positioning.IPositionObserver
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.positioning.PositioningService
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

class SimpleMapViewViewModel : ViewModel(), MvvmCustomViewModel<SimplifiedMapViewState> {

    private val logger = LoggerFactory.getLogger("MapViewViewModel")

    // region backing services
    private lateinit var routingService: RoutingService
    private lateinit var navigationService: NavigationService
    private lateinit var positioningService: PositioningService
    //endregion

    // region backing state
    override var state: SimplifiedMapViewState = SimplifiedMapViewState()
    // endregion

    // region initialization

    fun initialize(ionavContainer: IonavContainer) {
        routingService = ionavContainer.routingService
        positioningService = ionavContainer.positioningService
        navigationService = ionavContainer.navigationService


        navigationService.registerObserver(object : NavigationService.NavigationServiceObserver {
            override fun update(value: Coordinate?) {
                state.endCoordinate = value
                _destination.value = value
                logger.info("Updated destination to $value in view model.")
            }

            override fun update(remainingRoute: PathWrapper?) {
                this@SimpleMapViewViewModel._route.value = remainingRoute
            }
        })

        routingService.registerObserver(object : RoutingService.RoutingServiceStatusObserver {
            override fun update(v: RoutingService.Status) {
                _routingStatus.value = v
                updateInitializationStatus()
            }
        })

        positioningService.registerObserver(object : IPositionObserver {
            override fun update(t: IonavLocation?) {
                _userLocationLiveData.value = t
                _userLocation = t
            }
        })


        loadIndoorData(ionavContainer.osmFilePath)
    }


    private val _routingStatus: MutableLiveData<RoutingService.Status> = MutableLiveData(RoutingService.Status.UNINITIALIZED)
    val routingStatus: LiveData<RoutingService.Status> = _routingStatus


    private fun updateInitializationStatus() {
        // FIXME initialized = places loaded
        val routingStatusReady = _routingStatus.value == RoutingService.Status.READY

        val allReady = routingStatusReady
                && _isIndoorDataLoaded

        _initializationStatus.value =
            if (allReady) InitializationStatus.INITIALIZED
            else InitializationStatus.UNINITIALIZED
    }


    private val _initializationStatus: MutableLiveData<InitializationStatus> = MutableLiveData(InitializationStatus.UNINITIALIZED)
    val initializationStatus: LiveData<InitializationStatus> = _initializationStatus

    enum class InitializationStatus {
        UNINITIALIZED, INITIALIZED
    }
    // endregion


    // region destination
    private val _destination: MutableLiveData<Coordinate?> = MutableLiveData()

    val destination: LiveData<Coordinate?> = _destination

    fun setDestination(value: Coordinate?) {
        navigationService.destination = value
    }

    // endregion

    // region user location
    private var _userLocation: IonavLocation? = null
    private val _userLocationLiveData: MutableLiveData<IonavLocation?> = MutableLiveData()
    val userLocation: LiveData<IonavLocation?> = _userLocationLiveData

    private val followUserPosition: MutableLiveData<Boolean> = MutableLiveData(false)
    fun getFollowUserPositionLiveData(): LiveData<Boolean> = followUserPosition
    fun setFollowUserPosition(b: Boolean) {
        followUserPosition.value = b

        if (b) centerOnUserPos()
    }
    // endregion

    // region map center
    private fun centerOnUserPos() {
        setMapCenter(_userLocation ?: return)
    }


    private val _mapCenter: MutableLiveData<Coordinate?> = MutableLiveData(null)
    val mapCenter = _mapCenter
    private fun setMapCenter(c: IonavLocation) {
        _mapCenter.value = c
    }

    // endregion

    // region route
    private val _route: MutableLiveData<PathWrapper?> = MutableLiveData()
    val route: LiveData<PathWrapper?> = _route
    // endregion


    // region level
    private val _selectedLevel = MutableLiveData(0)
    val selectedLevel: LiveData<Int> = _selectedLevel

    fun getSelectedLevel(): Int = _selectedLevel.value ?: 0


    fun increaseLevel() {
        val oldValue = _selectedLevel.value ?: 0
        setLevel(oldValue + 1)
    }

    fun decreaseLevel() {
        val oldValue = _selectedLevel.value ?: 0
        setLevel(oldValue - 1)
    }

    private fun setLevel(newValue: Int) {
        _selectedLevel.value = newValue
    }
    // endregion


    // region indoor data
    private val _indoorData = MutableLiveData<IndoorData>(IndoorData.empty())
    val indoorData: LiveData<IndoorData> = _indoorData

    private var _isIndoorDataLoaded = false

    private fun loadIndoorData(osmFilePath: String) {
        val callback = object : IndoorMapDataLoadingTask.OnIndoorMapDataLoaded {
            override fun loadCompleted(indoorData: IndoorData) {
                _indoorData.value = indoorData
                _isIndoorDataLoaded = true
                logger.info("Completed loading of indoor map data.")
            }
        }

        IndoorMapDataLoadingTask(osmFilePath, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        logger.info("Started loading of indoor map data.")
    }
    // endregion
}