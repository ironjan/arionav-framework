package de.ironjan.arionav_fw.samples.campus.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.MvvmCustomViewModel
import de.ironjan.arionav_fw.ionav.di.IonavContainer
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.services.PositioningServiceState
import de.ironjan.arionav_fw.ionav.util.Observer
import de.ironjan.arionav_fw.ionav.viewmodel.IonavContainerDependentClass

class LocationHistoryViewModel : ViewModel(), MvvmCustomViewModel, IonavContainerDependentClass {

    // region initialization
    private lateinit var ionavContainer: IonavContainer
    private val positioningService by lazy { ionavContainer.positioningService }

    /**
     * Will initialize this view model. Does nothing, if {@param ionavContainer} is already known.
     * When overriding, you should call super first.
     */
    @CallSuper
    override fun initialize(ionavContainer: IonavContainer) {
        if (this::ionavContainer.isInitialized) return

        this.ionavContainer = ionavContainer

        positioningService.registerObserver(positioningServiceLocationHistoryObserver)
    }
    // endregion

    // region location history live data
    private val _locationHistoryLiveData = MutableLiveData(emptyList<IonavLocation>())
    val locationHistory: LiveData<List<IonavLocation>> = _locationHistoryLiveData

    // endregion
    // region observer
    val positioningServiceLocationHistoryObserver = object : Observer<PositioningServiceState> {
        override fun update(state: PositioningServiceState) {
            _locationHistoryLiveData.value = positioningService.locationHistory
        }

    }
    // endregion

}