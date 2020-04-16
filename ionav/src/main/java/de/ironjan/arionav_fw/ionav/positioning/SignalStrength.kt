package de.ironjan.arionav_fw.ionav.positioning

import de.ironjan.graphhopper.extensions_core.Coordinate

/**
 * Utility class for triangulation and to provide information for debug ui
 * @param deviceId the device's UUID
 * @param deviceId the device's name if it's a known evice
 * @param coordinate the device's coordinate if it's a known device
 * @param rssi the device's rssi value
 */
data class SignalStrength(val deviceId: String,
                          val name: String?,
                          val coordinate: Coordinate?,
                          val rssi: Int)