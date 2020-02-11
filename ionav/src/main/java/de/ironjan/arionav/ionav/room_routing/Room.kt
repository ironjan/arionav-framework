package de.ironjan.arionav.ionav.room_routing

import de.ironjan.graphhopper.extensions_core.Coordinate

data class Room(val name: String,
                val doors: List<Coordinate>)