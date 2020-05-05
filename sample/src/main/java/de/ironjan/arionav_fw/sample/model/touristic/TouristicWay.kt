package de.ironjan.arionav_fw.sample.model.touristic

import de.ironjan.arionav_fw.ionav.model.osm.PrimaryCoordinate
import de.ironjan.arionav_fw.ionav.model.osm.Way
import de.ironjan.graphhopper.extensions_core.Coordinate


/** Represents a simplified osm way. */
data class TouristicWay(
    val id: Long,
    val nodes: List<TouristicNode>,
    val tags: Map<String, String>
) : PrimaryCoordinate {
    val name = tags["name"] ?: ""

    private val lvl = tags["level"]?.toDoubleOrNull() ?: 0.0


    override val mainCoordinate: Coordinate
        get() =
            if (nodes.first().id.equals(nodes.last().id)) {
                val centerLat: Double = nodes.map { it.lat }.sum() / nodes.count()
                val centerLon: Double = nodes.map { it.lon }.sum() / nodes.count()
                Coordinate(centerLat, centerLon, lvl)
            } else Coordinate(nodes.first().lat, nodes.first().lon, lvl)

}