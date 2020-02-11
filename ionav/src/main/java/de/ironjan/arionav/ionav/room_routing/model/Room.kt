package de.ironjan.arionav.ionav.room_routing.model

import de.ironjan.graphhopper.extensions_core.Coordinate

/** Represents a simple room with doors at the given coordinates. */
data class Room(val name: String,
                val doors: List<Coordinate>)