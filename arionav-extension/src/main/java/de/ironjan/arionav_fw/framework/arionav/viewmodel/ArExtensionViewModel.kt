package de.ironjan.arionav_fw.framework.arionav.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.IonavContainer
import de.ironjan.arionav_fw.ionav.navigation.NavigationService
import de.ironjan.arionav_fw.ionav.positioning.PositioningService

/**
 * Serves as a wrapper around {@see MapViewViewModel} and provides access to relevant fields. Used in Activities that
 * provide both map-based and AR navigation.
 *
 * This approach does not follow best practices (view models should access a shared data source, not each other)
 */
class ArExtensionViewModel: ViewModel() {

    private lateinit var navigationService: NavigationService

    private lateinit var positioningService: PositioningService

    fun initialize(ionavContainer: IonavContainer) {
        positioningService = ionavContainer.positioningService
        navigationService = ionavContainer.navigationService
    }



    private val _remainingRoute: MutableLiveData<PathWrapper?> = MutableLiveData()
    val remainingRoute: LiveData<PathWrapper?> = _remainingRoute
}