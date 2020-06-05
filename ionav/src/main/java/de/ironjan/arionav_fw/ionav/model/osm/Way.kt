package de.ironjan.arionav_fw.ionav.model.osm

/** Represents a simplified osm way. */
open class Way(
    id: Long,
    val nodeRefs: List<Long>,
    tags: Map<String, String>
) : Element(id, tags)
