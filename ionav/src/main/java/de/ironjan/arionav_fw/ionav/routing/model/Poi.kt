package de.ironjan.arionav_fw.ionav.routing.model

import de.ironjan.graphhopper.extensions_core.Coordinate

/**
 * Represents a "Point of Interest".
 * @param name The room's name
 * @param coordinate Coordinate of the room's center point
 * @param tags The original osm tags
 */
data class Poi(
    override val name: String,
    override val coordinate: Coordinate,
    override val tags: Map<String, String>): NamedPlace