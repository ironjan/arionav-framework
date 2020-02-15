package de.ironjan.arionav.sample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.ironjan.arionav.ionav.mapview.MapViewViewModel

class SharedViewModel: ViewModel() {
    fun getRemainingRouteLiveData() = mapViewViewModel.getRemainingRouteLiveData()
    fun getCurrentRouteLiveData() = mapViewViewModel.getCurrentRouteLiveData()

    private lateinit var mapViewViewModel: MapViewViewModel

    fun setMapViewViewModel(vm: MapViewViewModel) {
        // FIXME this is a code smell!! layer arch says: shared source *below* VMs
        if(!(::mapViewViewModel.isInitialized)) {
            this.mapViewViewModel = vm
        }
    }

}