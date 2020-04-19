package de.ironjan.arionav_fw.ionav.model

import de.ironjan.graphhopper.extensions_core.Coordinate

/**
 * Represents a simple room.
 * @param name The room's name
 * @param coordinate Coordinate of the room's center point
 * @param doors The coordinates of the room's doors
 * @param tags The original osm tags
 */
data class Room(
    override val name: String,
    override val coordinate: Coordinate,
    override val tags: Map<String, String>,
    val doors: List<Coordinate>): NamedPlace