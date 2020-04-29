package de.ironjan.arionav_fw.ionav.services

import de.ironjan.arionav_fw.ionav.positioning.IonavLocation

data class PositioningServiceState(var lastKnownPosition: IonavLocation? = null,
                                   var lastUpdate: Long = -1L,
                                   var userSelectedLevel: Double = 0.0) {
}