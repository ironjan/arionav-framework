package de.ironjan.arionav_fw.ionav.model.indoor_map

import de.ironjan.graphhopper.extensions_core.Coordinate

/** Represents an indoor way. Can be a room, corridor, etc. */
data class IndoorWay(
    val id: Long,
    val lvl: Double,
    val nodeRefs: List<IndoorNode>,
    val tags: Map<String, String>
): IndoorPoi {

    private val doorCoordinate = nodeRefs.firstOrNull { it.tags["indoor"] == "door" }?.mainCoordinate

    val centerLat: Double = nodeRefs.map { it.lat }.sum() / nodeRefs.count()
    val centerLon: Double = nodeRefs.map { it.lon }.sum() / nodeRefs.count()
    override val mainCoordinate = doorCoordinate ?: Coordinate(centerLat, centerLon, lvl)

    val type: String = tags["indoor"] ?: ""
    val name: String = tags["name"] ?: ""

    val isRoom: Boolean = "room" == type
}