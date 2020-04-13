package de.ironjan.arionav_fw.ionav.routing.model.indoor_map

class IndoorData(
    val indoorWays: Map<Double, List<IndoorWay>>,
    val indoorNodes: Map<Double, List<IndoorNode>>
) {
    fun getWays(lvl: Double): List<IndoorWay> = indoorWays[lvl] ?: emptyList()

    fun getNodes(lvl: Double): List<IndoorNode> = indoorNodes[lvl] ?: emptyList()
}