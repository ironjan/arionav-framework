package de.ironjan.arionav_fw.ionav.model.indoor_map

import de.ironjan.graphhopper.extensions_core.Coordinate

data class IndoorWay(
    val id: Long,
    val lvl: Double,
    val nodeRefs: List<IndoorNode>,
    val tags: Map<String, String>
): IndoorPoi {

    val centerLat: Double = nodeRefs.map { it.lat }.sum() / nodeRefs.count()
    val centerLon: Double = nodeRefs.map { it.lon }.sum() / nodeRefs.count()
    override val center = Coordinate(centerLat, centerLon, lvl)

    val type: String = tags["indoor"] ?: ""
    val name: String = tags["name"] ?: ""

    val isRoom: Boolean = "room" == type
}