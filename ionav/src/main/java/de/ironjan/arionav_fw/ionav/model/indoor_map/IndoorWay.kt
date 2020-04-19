package de.ironjan.arionav_fw.ionav.model.indoor_map

data class IndoorWay(
    val id: Long,
    val lvl: Double,
    val nodeRefs: List<IndoorNode>,
    val tags: Map<String, String>
) {

    val centerLat: Double = nodeRefs.map { it.lat }.sum() / nodeRefs.count()
    val centerLon: Double = nodeRefs.map { it.lon }.sum() / nodeRefs.count()

    val type: String = tags["indoor"] ?: ""
    val name: String = tags["name"] ?: ""

    val isRoom: Boolean = "room" == type
}