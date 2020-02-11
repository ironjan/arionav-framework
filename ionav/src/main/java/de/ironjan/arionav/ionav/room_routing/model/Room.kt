package de.ironjan.arionav.ionav.room_routing.model

import de.ironjan.graphhopper.extensions_core.Coordinate

/** Represents a simple room.
 * @param name The room's name
 * @param coordinate Coordinate of the room's center point
 * @param doors The coordinates of the room's doors
 */
data class Room(val name: String,
                val coordinate: Coordinate,
                val doors: List<Coordinate>)