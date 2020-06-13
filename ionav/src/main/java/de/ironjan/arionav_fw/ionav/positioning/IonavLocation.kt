package de.ironjan.arionav_fw.ionav.positioning

import android.location.Location
import de.ironjan.graphhopper.extensions_core.Coordinate

class IonavLocation(
    provider: String,
    val coordinate: Coordinate
) : Location(provider) {

    constructor(provider: String, coordinate: Coordinate, location: Location): this(provider, coordinate) {
        set(location)
    }

    val lat = coordinate.lat
    val lon = coordinate.lon
    val lvl = coordinate.lvl

    val timestamp: Long = System.currentTimeMillis()

}