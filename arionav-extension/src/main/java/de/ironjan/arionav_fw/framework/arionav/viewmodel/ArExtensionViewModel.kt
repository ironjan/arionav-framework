package de.ironjan.arionav_fw.framework.arionav.viewmodel

import androidx.lifecycle.ViewModel
import de.ironjan.arionav_fw.ionav.mapview.MapViewViewModel

/**
 * Serves as a wrapper around {@see MapViewViewModel} and provides access to relevant fields. Used in Activities that
 * provide both map-based and AR navigation.
 *
 * This approach does not follow best practices (view models should access a shared data source, not each other)
 */
class ArExtensionViewModel: ViewModel() {
    fun getRemainingRouteLiveData() = mapViewViewModel.getRemainingRouteLiveData()

    private lateinit var mapViewViewModel: MapViewViewModel

    fun setMapViewViewModel(vm: MapViewViewModel) {
        // FIXME this is a code smell!! layer arch says: shared source *below* VMs
        if(!(::mapViewViewModel.isInitialized)) {
            this.mapViewViewModel = vm
        }
    }

}