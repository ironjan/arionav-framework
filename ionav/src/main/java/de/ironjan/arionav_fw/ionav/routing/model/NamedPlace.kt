package de.ironjan.arionav_fw.ionav.routing.model

import de.ironjan.graphhopper.extensions_core.Coordinate

/**
 * Represents a named place of some kind.
 */
interface NamedPlace {
    /** The place's name */
    val name: String
    /** The place's center point */
    val coordinate: Coordinate
    /** The place's original osm tags */
    val tags: Map<String, String>
}