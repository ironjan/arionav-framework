package de.ironjan.arionav_fw.ionav.positioning

data class PositioningServiceState(var lastKnownPosition: IonavLocation? = null,
                                   var lastUpdate: Long = -1L,
                                   var userSelectedLevel: Double = 0.0) {
}