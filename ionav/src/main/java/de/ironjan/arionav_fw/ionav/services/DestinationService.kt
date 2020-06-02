package de.ironjan.arionav_fw.ionav.services

import de.ironjan.graphhopper.extensions_core.Coordinate

abstract class DestinationService {
    /**
     * Tries to convert {@code value} into a {@code Coordinate}.
     *
     * @return the coordinate of the given place or {@code null}
     */
    abstract fun getCoordinate(value: String): Coordinate?
}