package de.ironjan.arionav_fw.ionav.model.osm

/**
 * Represents a simplified osm node.
 */
open class Node(
    id: Long,
    val lat: Double,
    val lon: Double,
    tags: Map<String, String>
) : Element(id, tags)