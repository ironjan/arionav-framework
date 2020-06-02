package de.ironjan.arionav_fw.ionav.services

import de.ironjan.graphhopper.extensions_core.Coordinate

class IndoorDestinationService(private val indoorDataService: IndoorDataService) {

    /**
     * Tries to convert {@code value} into a {@code Coordinate}.
     *
     * @return the coordinate of the given place or {@code null}
     */
    fun getCoordinate(value: String): Coordinate? {
        val parsedAttempt = try {
            Coordinate.fromString(value)
        } catch (_: Exception) {
            null
        }
        return parsedAttempt ?: indoorDataService.indoorData.getCoordinateOf(value)
    }

}