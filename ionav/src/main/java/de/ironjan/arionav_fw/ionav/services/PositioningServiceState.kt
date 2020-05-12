package de.ironjan.arionav_fw.ionav.services

import de.ironjan.arionav_fw.ionav.positioning.IonavLocation

data class PositioningServiceState(val lastKnownPosition: IonavLocation? = null,
                                   val lastUpdate: Long = -1L,
                                   val userSelectedLevel: Double = 0.0) {
}