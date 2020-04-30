package de.ironjan.arionav_fw.ionav.model.indoor_map

import de.ironjan.graphhopper.extensions_core.Coordinate

/** Common interface for indoor elements with a {@see center} coordinate */
interface IndoorPoi {
    val center : Coordinate
}