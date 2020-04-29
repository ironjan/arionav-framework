package de.ironjan.arionav_fw.ionav.navigation

import com.graphhopper.PathWrapper
import de.ironjan.graphhopper.extensions_core.Coordinate

data class NavigationServiceState(var destination: Coordinate?, var remainingRoute: PathWrapper?)

