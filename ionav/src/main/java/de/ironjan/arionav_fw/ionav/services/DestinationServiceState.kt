package de.ironjan.arionav_fw.ionav.services

import de.ironjan.graphhopper.extensions_core.Coordinate

data class DestinationServiceState(val destinations: Map<String, Coordinate>)