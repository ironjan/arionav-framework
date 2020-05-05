package de.ironjan.arionav_fw.sample.model.touristic

import de.ironjan.arionav_fw.ionav.model.osm.PrimaryCoordinate
import de.ironjan.graphhopper.extensions_core.Coordinate

data class TouristicNode(val id: Long,
                val lat: Double,
                val lon: Double,
                val tags: Map<String, String>): PrimaryCoordinate {
    val name = tags["name"] ?: ""

    private val lvl = tags["level"]?.toDoubleOrNull() ?: 0.0

    override val mainCoordinate: Coordinate = Coordinate(lat, lon, lvl)
}