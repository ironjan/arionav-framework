package de.ironjan.arionav.ionav.positioning

import de.ironjan.graphhopper.extensions_core.Coordinate

/**
 * Utility class for triangulation.
 * @param deviceId the device's UUID
 * @param coordinate the device's coordinate, if it's a known device
 * @param rssi the device's rssi value
 */
data class SignalStrength(val deviceId: String,
                          val coordinate: Coordinate?,
                          val rssi: Int)