package de.ironjan.arionav_fw.sample.viewmodel

import androidx.lifecycle.ViewModel
import de.ironjan.arionav_fw.ionav.mapview.MapViewViewModel

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