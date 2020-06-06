package de.ironjan.arionav_fw.ionav.model.indoor_map

import de.ironjan.arionav_fw.ionav.model.osm.Way
import de.ironjan.graphhopper.extensions_core.Coordinate


/** Represents an indoor way. Can be a room, corridor, etc. */
open class IndoorWay(
    id: Long,
    val lvl: Double,
    val nodes: List<IndoorNode>,
    tags: Map<String, String>
): Way(id,nodes.map {it.id }, tags), IndoorPoi {
    val distinctNodeRefs = nodes.distinct()

    val centerLat: Double = distinctNodeRefs.map { it.lat }.sum() / distinctNodeRefs.count()
    val centerLon: Double = distinctNodeRefs.map { it.lon }.sum() / distinctNodeRefs.count()
    override val center = Coordinate(centerLat, centerLon, lvl)

    val type: String = tags["indoor"] ?: ""

    val isRoom: Boolean = "room" == type
    val isCorridor: Boolean = "corridor" == type
    val isArea: Boolean = "area" == type
    val isFloorConnector: Boolean =
        tags["stairs"] ?: "no" == "yes"
                || tags["highway"] ?: "" == "elevator"
                || tags["highway"] ?: "" == "steps"

    private val tagsAsString = tags.map{"${it.key}=${it.value}"}.joinToString(",")
    override fun toString(): String {
        return "IndoorWay($id, $lvl, $tagsAsString, $nodes)"
    }
}