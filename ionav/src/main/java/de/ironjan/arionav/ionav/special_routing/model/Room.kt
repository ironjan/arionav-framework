package de.ironjan.arionav.ionav.special_routing.model

import de.ironjan.graphhopper.extensions_core.Coordinate

/** Represents a simple room.
 * @param name The room's name
 * @param coordinate Coordinate of the room's center point
 * @param doors The coordinates of the room's doors
 * @param doors The original osm tags
 */
data class Room(val name: String,
                val coordinate: Coordinate,
                val doors: List<Coordinate>,
                val tags: Map<String, String>)