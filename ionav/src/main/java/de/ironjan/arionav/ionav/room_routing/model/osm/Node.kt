package de.ironjan.arionav.ionav.room_routing.model.osm

/**
 * Represents a simplified osm node.
 */
data class Node(val id: Long,
                val lat: Double,
                val lon: Double,
                val tags: Map<String, String>)