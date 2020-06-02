package de.ironjan.arionav_fw.samples.tourism.services

import de.ironjan.arionav_fw.ionav.services.DestinationService
import de.ironjan.arionav_fw.ionav.services.DestinationServiceState
import de.ironjan.graphhopper.extensions_core.Coordinate

class TourismDestinationService : DestinationService() {
    override val state: DestinationServiceState
        get() = DestinationServiceState(destinations)

    override fun getCoordinate(value: String): Coordinate? {
        return null
    }

    fun init(osmFilePath: String) {

    }
}