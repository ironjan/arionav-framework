package de.ironjan.arionav_fw.ionav.model.osm

import de.ironjan.graphhopper.extensions_core.Coordinate

/** Common interface for elements to denote a primary coordinate.
 * Can be either the elements coordinate itself (for nodes), the center (for areas) */
interface PrimaryCoordinate {
    val mainCoordinate : Coordinate
}