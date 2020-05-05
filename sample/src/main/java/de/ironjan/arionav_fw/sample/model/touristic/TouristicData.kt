package de.ironjan.arionav_fw.sample.model.touristic

import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.model.osm.Way

class TouristicData(private val ways: List<Way>, private val nodes: List<Node>) {

    private val touristicNodes = nodes.map { TouristicNode(it.id, it.lat, it.lon, it.tags) }
    private val touristicNodeMap = touristicNodes.map { Pair(it.id, it) }.toMap()
    private val touristicWays =
        ways.map { w ->
            val wayNodes = w.nodeRefs.map { n -> touristicNodeMap[n]!! }
            TouristicWay(w.id, wayNodes, w.tags)
        }
    val names = nodes.map { it.name }
        .union(ways.map { it.name })
        .sorted()

    fun getCoordinateOf(name: String) =
        (touristicWays.firstOrNull { it.name == name } ?: touristicNodes.firstOrNull { it.name == name })?.mainCoordinate

}