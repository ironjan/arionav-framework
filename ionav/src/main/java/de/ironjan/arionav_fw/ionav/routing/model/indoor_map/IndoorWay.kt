package de.ironjan.arionav_fw.ionav.routing.model.indoor_map

data class IndoorWay(
    val id: Long,
    val lvl: Double,
    val nodeRefs: List<IndoorNode>,
    val tags: Map<String, String>
) {

    val type: String = tags["indoor"] ?: ""
    val name: String = tags["name"] ?: ""

    val isRoom: Boolean = "room" == type
}