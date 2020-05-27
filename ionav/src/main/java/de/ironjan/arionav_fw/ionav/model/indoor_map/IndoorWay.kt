package de.ironjan.arionav_fw.ionav.model.indoor_map

import de.ironjan.graphhopper.extensions_core.Coordinate


/** Represents an indoor way. Can be a room, corridor, etc. */
data class IndoorWay(
    val id: Long,
    val lvl: Double,
    val nodeRefs: List<IndoorNode>,
    val tags: Map<String, String>
): IndoorPoi {
    val distinctNodeRefs = nodeRefs.distinct()

    val centerLat: Double = distinctNodeRefs.map { it.lat }.sum() / distinctNodeRefs.count()
    val centerLon: Double = distinctNodeRefs.map { it.lon }.sum() / distinctNodeRefs.count()
    override val center = Coordinate(centerLat, centerLon, lvl)

    val type: String = tags["indoor"] ?: ""
    val name: String = tags["name"] ?: ""

    val isRoom: Boolean = "room" == type
    val isCorridor: Boolean = "corridor" == type
    val isArea: Boolean = "area" == type
    val isFloorConnector: Boolean =
        tags["stairs"] ?: "no" == "yes"
                || tags["highway"] ?: "" == "elevator"
                || tags["highway"] ?: "" == "steps"

    private val tagsAsString = tags.map{"${it.key}=${it.value}"}.joinToString(",")
    override fun toString(): String {
        return "IndoorWay($id, $lvl, $tagsAsString, $nodeRefs)"
    }
}