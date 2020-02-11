package de.ironjan.arionav.ionav.room_routing.model.osm

/** Represents a simplified osm way. */
data class Way(val id:Long,
               val nodeRefs: List<Long>,
               val tags: Map<String, String>)
