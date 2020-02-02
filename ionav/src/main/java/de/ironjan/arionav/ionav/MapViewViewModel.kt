package de.ironjan.arionav.ionav

import androidx.lifecycle.ViewModel
import com.graphhopper.PathWrapper
import de.ironjan.graphhopper.extensions_core.Coordinate

class MapViewViewModel() : ViewModel() {
    var startCoordinate: Coordinate? = null
    var endCoordinate: Coordinate? = null

    var currentRoute: PathWrapper? = null
    var currentUserPosition: Coordinate? = null
}