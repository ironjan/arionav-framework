package de.ironjan.arionav_fw.samples.tourism.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav_fw.ionav.di.IonavContainer
import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.services.DestinationServiceState
import de.ironjan.arionav_fw.ionav.util.Observer
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.arionav_fw.samples.tourism.services.TourismDestinationService
import de.ironjan.arionav_fw.samples.tourism.services.TourismDestinationServiceState

class TourismViewModel: IonavViewModel() {
    private val _destinationNodes = MutableLiveData<Map<String, Node>>(emptyMap())
    val pois: LiveData<Map<String, Node>> = _destinationNodes

    override fun initialize(ionavContainer: IonavContainer) {
        super.initialize(ionavContainer)

        (ionavContainer.destinationService as? TourismDestinationService)?.registerObserver(object : Observer<DestinationServiceState>{
            override fun update(state: DestinationServiceState) {
                // since destinationService is a TourismDestinationService, the received data should be TourismDestinationServiceState
                when(state) {
                    is TourismDestinationServiceState -> {
                        _destinationNodes.value = state.destinationNodes
                    }
                }
            }
        })
    }
}