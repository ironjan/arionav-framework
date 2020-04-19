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
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.positioning.PositioningService
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

class SimpleMapViewViewModel : ViewModel(), MvvmCustomViewModel<SimplifiedMapViewState> {

    private lateinit var routingService: RoutingService
    private lateinit var navigationService: NavigationService

    private lateinit var positioningService: PositioningService

    fun initialize(ionavContainer: IonavContainer) {
        routingService = ionavContainer.routingService
        positioningService = ionavContainer.positioningService
        navigationService = ionavContainer.navigationService


        navigationService.registerObserver(object : NavigationService.NavigationServiceObserver {
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
                updateInitializationStatus()
            }
        })


        loadIndoorData(ionavContainer.osmFilePath)
    }


    // region initialization status
    private val _routingStatus: MutableLiveData<RoutingService.Status> = MutableLiveData(RoutingService.Status.UNINITIALIZED)
    val routingStatus: LiveData<RoutingService.Status> = _routingStatus


    private fun updateInitializationStatus() {
        // FIXME initialized = indoor here, places loaded
        val routingStatusReady = _routingStatus.value == RoutingService.Status.READY
        _initializationStatus.value =
            if (routingStatusReady) InitializationStatus.INITIALIZED
            else InitializationStatus.UNINITIALIZED
    }


    private val _initializationStatus: MutableLiveData<InitializationStatus> = MutableLiveData(InitializationStatus.UNINITIALIZED)
    val initializationStatus: LiveData<InitializationStatus> = _initializationStatus

    enum class InitializationStatus {
        UNINITIALIZED, INITIALIZED
    }
    // endregion


    override var state: SimplifiedMapViewState = SimplifiedMapViewState()


    private val endCoordinate: MutableLiveData<Coordinate?> = MutableLiveData()

    fun getEndCoordinateLifeData(): LiveData<Coordinate?> = endCoordinate

    fun setDestination(value: Coordinate?) {
        navigationService.destination = value
    }


    fun getUserPositionLiveData(): LiveData<IonavLocation?> = positioningService.lastKnownLocation


    private val remainingRoute: MutableLiveData<PathWrapper?> = MutableLiveData()
    fun getRemainingRouteLiveData(): LiveData<PathWrapper?> = remainingRoute


    private val followUserPosition: MutableLiveData<Boolean> = MutableLiveData(false)
    fun getFollowUserPositionLiveData(): LiveData<Boolean> = followUserPosition
    fun setFollowUserPosition(b: Boolean) {
        followUserPosition.value = b

        if (b) centerOnUserPos()
    }


    private val logger = LoggerFactory.getLogger("MapViewViewModel")


    private val mapCenter: MutableLiveData<Coordinate?> = MutableLiveData(null)
    fun getMapCenterLiveData(): LiveData<Coordinate?> = mapCenter
    private fun setMapCenter(c: IonavLocation) {
        mapCenter.value = c
    }

    private fun centerOnUserPos() {
        setMapCenter(getUserPositionLiveData().value ?: return)
    }


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
    val _indoorData = MutableLiveData<IndoorData>(IndoorData.empty())
    val indoorData: LiveData<IndoorData> = _indoorData


    private fun loadIndoorData(osmFilePath: String) {
        val callback = object : IndoorMapDataLoadingTask.OnIndoorMapDataLoaded {
            override fun loadCompleted(indoorData: IndoorData) {
                _indoorData.value = indoorData
                logger.info("Completed loading of indoor map data.")
            }
        }

        IndoorMapDataLoadingTask(osmFilePath, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        logger.info("Started loading of indoor map data.")
    }
    // endregion
}