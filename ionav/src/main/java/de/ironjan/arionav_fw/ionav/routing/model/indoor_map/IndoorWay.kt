package de.ironjan.arionav_fw.ionav.routing.model.indoor_map

data class IndoorWay(
    val id: Long,
    val lvl:Double,
    val type: String,
    val nodeRefs: List<IndoorNode>,
    val tags: Map<String, String>)