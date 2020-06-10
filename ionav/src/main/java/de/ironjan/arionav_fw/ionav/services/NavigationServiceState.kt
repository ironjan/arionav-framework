package de.ironjan.arionav_fw.ionav.services

import com.graphhopper.PathWrapper
import de.ironjan.graphhopper.extensions_core.Coordinate

data class NavigationServiceState(val destination: Coordinate? = null,
                                  val remainingRoute: PathWrapper? = null) {
    val remainingDistance = remainingRoute?.distance
}

