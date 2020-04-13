package de.ironjan.arionav_fw.ionav.routing.model.osm

/** Represents a simplified osm way. */
data class Way(val id:Long,
               val nodeRefs: List<Long>,
               val tags: Map<String, String>)