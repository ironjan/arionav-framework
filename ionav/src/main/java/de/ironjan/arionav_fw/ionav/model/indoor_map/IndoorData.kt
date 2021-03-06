package de.ironjan.arionav_fw.ionav.model.indoor_map

import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.model.osm.Way
import org.slf4j.LoggerFactory

/**
 * An object containing indoor osm elements like {@see IndoorNode} and {@see IndoorWay}.
 */
class IndoorData(
    ways: List<Way>,
    nodes: List<Node>
) {
    // region private properties
    private val indoorNodes = nodes.map {
        IndoorNode(it.id, it.lat, it.lon, it.tags)
    }

    private val indoorWays = ways.map {
        val lvl = it.tags["level"]?.toDoubleOrNull() ?: 0.0
        val nodeRefs = it.nodeRefs.mapNotNull { nr ->
            indoorNodes.find { n -> n.id == nr }
        }
        IndoorWay(it.id, lvl, nodeRefs, it.tags)
    }
    private val indoorWaysByLevel = indoorWays.groupBy { it.lvl }

    private val indoorNodesByLevel = indoorNodes.groupBy { it.lvl }

    // endregion

    val destinations = indoorNodes.map { Pair(it.name, it.center )}
        .union( indoorWays.map { Pair(it.name, it.center) })
        .filterNot { it.first.isNullOrEmpty() }
        .map { Pair(it.first!! ,it.second)}
        .toMap()

    val names = indoorNodes.map { it.name }
        .union(indoorWays.map { it.name })
        .filterNot{ it.isNullOrEmpty() }




    val levels =
        indoorNodesByLevel.flatMap { it.value }.map { it.lvl }.union(
            indoorWaysByLevel.flatMap { it.value }.map { it.lvl }
        ).distinct()

    fun getWays(lvl: Double): List<IndoorWay> = indoorWaysByLevel[lvl] ?: emptyList()

    fun getNodes(lvl: Double): List<IndoorNode> = indoorNodesByLevel[lvl] ?: emptyList()

    fun getCoordinateOf(name: String) =
        ((indoorWays.firstOrNull { it.name == name }
                         ?: indoorNodes.firstOrNull { it.name == name })
                         as? IndoorPoi) 
                ?.center


    companion object {
        fun empty() = IndoorData(emptyList(), emptyList())
    }
}