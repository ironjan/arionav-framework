package de.ironjan.arionav_fw.ionav.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.graphhopper.PathWrapper
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.MvvmCustomViewModel
import de.ironjan.arionav_fw.ionav.di.IonavContainer
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.services.*
import de.ironjan.arionav_fw.ionav.util.Observer
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

open class IonavViewModel : ViewModel(), MvvmCustomViewModel, IonavContainerDependentClass {

    private val logger = LoggerFactory.getLogger("IonavViewModel")

    // region backing services
    private lateinit var ionavContainer: IonavContainer

    private val routingService by lazy { ionavContainer.routingService }
    private val navigationService by lazy { ionavContainer.navigationService }
    private val positioningService by lazy { ionavContainer.positioningService }
    private val destinationService by lazy { ionavContainer.destinationService }
    private val indoorDataService by lazy { ionavContainer.indoorDataService }

    val instructionHelper by lazy { ionavContainer.instructionHelper }

    val mapFilePath by lazy { ionavContainer.mapFilePath }
    //endregion


    // region initialization

    /**
     * Will initialize this view model. Does nothing, if {@param ionavContainer} is already known.
     * When overriding, you should call super first.
     */
    @CallSuper
    override fun initialize(ionavContainer: IonavContainer) {
        if (this::ionavContainer.isInitialized) return

        this.ionavContainer = ionavContainer

        navigationService.registerObserver(object : Observer<NavigationServiceState> {
            override fun update(state: NavigationServiceState) {
                val remainingRoute = state.remainingRoute

                _destination.value = state.destination

                val routeHasError = remainingRoute?.hasErrors() != false
                if(routeHasError) {
                    _route.value = null
                    _remainingDistanceToDestination.value = null
                    _remainingDurationToDestination.value = null
                }else {
                    _remainingDistanceToDestination.value = remainingRoute?.distance
                    _remainingDurationToDestination.value = remainingRoute?.time
                    _route.value = remainingRoute
                }

                logger.info("Received navigation service update: $state.")
            }

        })

        _routingStatus.value = routingService.status
        routingService.registerObserver(object : Observer<RoutingServiceState> {
            override fun update(state: RoutingServiceState) {
                _routingStatus.value = state.status
                updateInitializationStatus()
            }
        })

        positioningService.registerObserver(object : Observer<PositioningServiceState> {
            override fun update(state: PositioningServiceState) {
                _userLocation = state.lastKnownPosition
                _userLocationLiveData.value = _userLocation
            }

        })

        indoorDataService.registerObserver(object : Observer<IndoorDataState> {
            override fun update(state: IndoorDataState) {
                _indoorData.value = state.indoorData
                updateInitializationStatus()
            }
        })

        destinationService.registerObserver(object : Observer<DestinationServiceState> {
            override fun update(state: DestinationServiceState) {
                _destinations.value = state.destinations
            }
        })

        logger.info("Started loading of indoor map data.")
    }


    private val _routingStatus: MutableLiveData<RoutingService.Status> = MutableLiveData(RoutingService.Status.UNINITIALIZED)
    val routingStatus: LiveData<RoutingService.Status> = _routingStatus


    private fun updateInitializationStatus() {
        val routingStatusReady = _routingStatus.value == RoutingService.Status.READY
        val indoorDataServiceReady = indoorDataService.loadingState == IndoorDataLoadingState.READY

        val allReady = routingStatusReady
                && indoorDataServiceReady

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

    private val _destinationString: MutableLiveData<String> = MutableLiveData()
    val destinationString: LiveData<String> = _destinationString

    fun setDestination(value: Coordinate?) {
        navigationService.destination = value

        if(value == null) return

        val oldDestinationString = _destinationString.value
        val newDestinationString = value.asString() ?: oldDestinationString
        _destinationString.value = newDestinationString
    }

    /**
     * Sets the destination string an retrieves its coordinates if possible.
     *
     * @return the coordinates of `name` or `null`
     */
    fun setDestinationString(name: String): Coordinate? {
        _destinationString.value = name

        return getCoordinateOf(name)
    }

    fun getCoordinateOf(name: String): Coordinate? {
        return destinationService.getCoordinate(name) ?: return null
    }


    fun setDestinationAndName(name: String, coordinate: Coordinate) {
        _destinationString.value = name
        navigationService.destination = coordinate
        centerOnUserPos()

    }

    private val _destinations = MutableLiveData<Map<String,Coordinate>>(emptyMap())
    val destinations: LiveData<Map<String,Coordinate>> = _destinations

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

    val isFollowUser
        get() = followUserPosition.value ?: false
    // endregion

    // region map center
    private fun centerOnUserPos() {
        followUserPosition.value = true
        setMapCenter(_userLocation ?: return)
    }


    private val _mapCenter: MutableLiveData<Coordinate?> = MutableLiveData(null)
    val mapCenter = _mapCenter
    private fun setMapCenter(c: IonavLocation) {
        _mapCenter.value = c.coordinate
    }

    // endregion

    // region route
    private val _route: MutableLiveData<PathWrapper?> = MutableLiveData()
    val route: LiveData<PathWrapper?> = _route

    private val _remainingDistanceToDestination: MutableLiveData<Double?> = MutableLiveData()
    val remainingDistanceToDestination: LiveData<Double?> = _remainingDistanceToDestination

    private val _remainingDurationToDestination: MutableLiveData<Long?> = MutableLiveData()
    val remainingDurationToDestination: LiveData<Long?> = _remainingDurationToDestination

    val currentInstruction by lazy {
        val mediatorLiveData = MediatorLiveData<Instruction?>()
        mediatorLiveData.addSource(_route) {
            if (it == null) {
                mediatorLiveData.value = null
                return@addSource
            }

            if (it.hasErrors()) mediatorLiveData.value = null

            mediatorLiveData.value = it.instructions.firstOrNull()
        }
        mediatorLiveData
    }
    val nextInstruction by lazy {
        val mediatorLiveData = MediatorLiveData<Instruction?>()
        mediatorLiveData.addSource(_route) {
            if (it == null) {
                mediatorLiveData.value = null
                return@addSource
            }

            if (it.hasErrors()) mediatorLiveData.value = null

            mediatorLiveData.value = it.instructions.drop(1).firstOrNull()
        }
        mediatorLiveData
    }

    val instructionText by lazy {
        val mediatorLiveData = MediatorLiveData<String?>()
        mediatorLiveData.addSource(route) {
            if (it == null || it.hasErrors()) {
                mediatorLiveData.value = null
                return@addSource
            }

            val instructions = it.instructions
            val current = instructions.firstOrNull() ?: return@addSource
            val next = instructions.drop(1).firstOrNull()

            mediatorLiveData.value = InstructionHelper(ionavContainer.applicationContext).toText(current, next)
        }
        mediatorLiveData
    }
    // endregion


    // region level
    private val _selectedLevel = MutableLiveData(0.0)
    val selectedLevel: LiveData<Double> = _selectedLevel

    fun getSelectedLevel() = _selectedLevel.value ?: 0.0


    fun increaseLevel() {
        val oldValue = _selectedLevel.value ?: 0.0
        setSelectedLevel(oldValue + 1.0)
    }

    fun decreaseLevel() {
        val oldValue = _selectedLevel.value ?: 0.0
        setSelectedLevel(oldValue - 1.0)
    }

    fun setSelectedLevel(newValue: Double) {
        positioningService.userSelectedLevel = newValue
        _selectedLevel.value = newValue
    }
    // endregion


    // region indoor data
    private val _indoorData = MutableLiveData<IndoorData>()
    val indoorData: LiveData<IndoorData> = _indoorData
    // endregion

    companion object {
        const val STATE_DESTINATION = "STATE_DESTINATION"
        const val STATE_DESTINATION_STRING = "STATE_DESTINATION_STRING"
    }
}