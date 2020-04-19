package de.ironjan.arionav_fw.ionav.model.indoor_map

class IndoorData(
    val indoorWays: Map<Double, List<IndoorWay>>,
    val indoorNodes: Map<Double, List<IndoorNode>>
) {
    val levels =
        indoorNodes.flatMap { it.value }.map { it.lvl }.union(
            indoorWays.flatMap { it.value }.map { it.lvl }
        ).distinct()

    fun getWays(lvl: Double): List<IndoorWay> = indoorWays[lvl] ?: emptyList()

    fun getNodes(lvl: Double): List<IndoorNode> = indoorNodes[lvl] ?: emptyList()
}