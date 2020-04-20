package de.ironjan.arionav_fw.ionav.model.indoor_map

import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.model.osm.Way
import org.slf4j.LoggerFactory

class IndoorData(
    private val ways: List<Way>,
    private val nodes: List<Node>
) {
    private val logger = LoggerFactory.getLogger(IndoorData::class.simpleName)
    private val indoorNodes = nodes.map {
        val lvl = it.tags["level"]?.toDoubleOrNull() ?: 0.0
        IndoorNode(it.id, it.lat, it.lon, lvl, it.tags)
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


    val names = indoorNodes.map { it.name }
        .union(indoorWays.map { it.name })



    val levels =
        indoorNodesByLevel.flatMap { it.value }.map { it.lvl }.union(
            indoorWaysByLevel.flatMap { it.value }.map { it.lvl }
        ).distinct()

    fun getWays(lvl: Double): List<IndoorWay> = indoorWaysByLevel[lvl] ?: emptyList()

    fun getNodes(lvl: Double): List<IndoorNode> = indoorNodesByLevel[lvl] ?: emptyList()

    fun getWayByName(name: String) = indoorWays.firstOrNull { it.name == name }

    companion object {
        fun empty() = IndoorData(emptyList(), emptyList())
    }
}