package de.ironjan.arionav.ionav.positioning

import de.ironjan.graphhopper.extensions_core.Coordinate

data class IonavLocation(
    val provider: String,
    val coordinate: Coordinate
)
// FIXME remove inheritance
    : Coordinate(coordinate.lat, coordinate.lon, coordinate.lvl) {
    val latL = coordinate.lat
    val lonL = coordinate.lon
    val lvlL = coordinate.lvl

    val timestamp: Long = System.currentTimeMillis()
}