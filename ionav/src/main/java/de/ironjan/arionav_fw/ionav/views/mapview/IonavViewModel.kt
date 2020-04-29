package de.ironjan.arionav_fw.ionav.views.mapview

import android.os.AsyncTask
import androidx.lifecycle.*
import com.graphhopper.PathWrapper
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.ionav.IonavContainer
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.MvvmCustomViewModel
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.model.readers.IndoorMapDataLoadingTask
import de.ironjan.arionav_fw.ionav.navigation.InstructionHelper
import de.ironjan.arionav_fw.ionav.navigation.NavigationService
import de.ironjan.arionav_fw.ionav.positioning.IPositionObserver
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import de.ironjan.arionav_fw.ionav.util.Observer
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

class IonavViewModel(override val state: SavedStateHandle) : ViewModel(), MvvmCustomViewModel {

    private val logger = LoggerFactory.getLogger("MapViewViewModel")

    // region backing services
    private lateinit var ionavContainer: IonavContainer

    val routingService by lazy {  ionavContainer.routingService }
    val navigationService by lazy {
        val service = ionavContainer.navigationService

        service.destination = readCoordinateFromState()

        service
    }

    val positioningService  by lazy { ionavContainer.positioningService }

    val mapFilePath by lazy { ionavContainer.mapFilePath }
    //endregion


    // region initialization

    /**
     * Will initialize this view model. Does nothing, if {@param ionavContainer} is already known.
     */
    fun initialize(ionavContainer: IonavContainer) {
        if(this::ionavContainer.isInitialized) return

        this.ionavContainer = ionavContainer

        navigationService.registerObserver(object : NavigationService.NavigationServiceObserver {
            override fun update(value: Coordinate?) {
                state.set(STATE_DESTINATION, value?.asString())
                _destination.value = value
                logger.info("Updated destination to $value in view model.")
            }

            override fun update(remainingRoute: PathWrapper?) {
                this@IonavViewModel._route.value = remainingRoute
            }
        })

        _routingStatus.value = routingService.status
        routingService.registerObserver(object : Observer<RoutingService.Status> {
            override fun update(v: RoutingService.Status) {
                _routingStatus.value = v
                updateInitializationStatus()
            }
        })

        positioningService.registerObserver(object : IPositionObserver {
            override fun update(t: IonavLocation?) {
                _userLocationLiveData.value = t
                _userLocation = t
                _locationHistoryLiveData.value = positioningService.locationHistory
            }
        })

        navigationService.destination = readCoordinateFromState()


        loadIndoorData(ionavContainer.osmFilePath)
    }



    private fun readCoordinateFromState(): Coordinate? {
        val get = state.get<String>(STATE_DESTINATION)
        val coordinate = if (get != null) Coordinate.fromString(get)
        else null
        return coordinate
    }
    private val _routingStatus: MutableLiveData<RoutingService.Status> = MutableLiveData(RoutingService.Status.UNINITIALIZED)
    val routingStatus: LiveData<RoutingService.Status> = _routingStatus


    private fun updateInitializationStatus() {
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

    private val _destinationString: MutableLiveData<String> = MutableLiveData(state.get(STATE_DESTINATION_STRING))
    val destinationString: LiveData<String> = _destinationString

    fun setDestination(value: Coordinate?) {
        navigationService.destination = value
        state.set(STATE_DESTINATION, value?.asString())

        val oldDestinationString = _destinationString.value
        val newDestinationString = value?.asString() ?: oldDestinationString
        _destinationString.value = newDestinationString
        state.set(STATE_DESTINATION_STRING,newDestinationString)
    }

    fun setDestinationString(value: String): Boolean {
        _destinationString.value = value
        state.set(STATE_DESTINATION_STRING,value)

        val center = _indoorData.value?.getCoordinateOf(value)
        val coordinate = center ?: return false
        state.set(STATE_DESTINATION, coordinate.asString())

        navigationService.destination = coordinate
        return true
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
    val isFollowUser = followUserPosition.value ?: false


    private val _locationHistoryLiveData = MutableLiveData(emptyList<IonavLocation>())
    val locationHistory: LiveData<List<IonavLocation>> = _locationHistoryLiveData

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

            val current =  it!!.instructions.firstOrNull() ?: return@addSource
            val next = it!!.instructions.drop(1).firstOrNull()

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
        setLevel(oldValue + 1.0)
    }

    fun decreaseLevel() {
        val oldValue = _selectedLevel.value ?: 0.0
        setLevel(oldValue - 1.0)
    }

    fun setLevel(newValue: Double) {
        positioningService.userSelectedLevel = newValue.toDouble()
        _selectedLevel.value = newValue
    }
    // endregion


    // region indoor data
    private val _indoorData = MutableLiveData<IndoorData>(IndoorData.empty())
    val indoorData: LiveData<IndoorData> = _indoorData

    private var _isIndoorDataLoaded = false

    private fun loadIndoorData(osmFilePath: String) {
        val callback = { loadedData: IndoorData ->
            _indoorData.value = loadedData
            _isIndoorDataLoaded = true
            updateInitializationStatus()
            logger.info("Completed loading of indoor map data.")
        }

        IndoorMapDataLoadingTask(osmFilePath, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        logger.info("Started loading of indoor map data.")
    }

    // endregion

    companion object {
        const val STATE_DESTINATION = "STATE_DESTINATION"
        const val STATE_DESTINATION_STRING = "STATE_DESTINATION_STRING"
    }
}