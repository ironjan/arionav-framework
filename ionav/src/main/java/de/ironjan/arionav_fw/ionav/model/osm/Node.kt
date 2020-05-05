package de.ironjan.arionav_fw.ionav.model.osm

import de.ironjan.graphhopper.extensions_core.Coordinate

/**
 * Represents a simplified osm node.
 */
data class Node(val id: Long,
                val lat: Double,
                val lon: Double,
                val tags: Map<String, String>) {
    val name = tags["name"] ?: ""
}