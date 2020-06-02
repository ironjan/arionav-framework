package de.ironjan.arionav_fw.ionav.services

import de.ironjan.arionav_fw.ionav.util.Observer
import de.ironjan.graphhopper.extensions_core.Coordinate

class IndoorDestinationService(private val indoorDataService: IndoorDataService) : DestinationService() {
    init {
        indoorDataService.registerObserver(object : Observer<IndoorDataState> {
            override fun update(state: IndoorDataState) {

                if(state.indoorDataLoadingState == IndoorDataLoadingState.READY) {
                    destinations = indoorDataService.indoorData.destinations
                    indoorDataService.removeObserver(this)
                }
            }
        })
    }


    override val state: DestinationServiceState
        get() = DestinationServiceState(destinations)

    override var destinations: Map<String, Coordinate> = emptyMap()

    /**
     * Tries to convert {@code value} into a {@code Coordinate}.
     *
     * @return the coordinate of the given place or {@code null}
     */
    override fun getCoordinate(value: String): Coordinate? {
        val parsedAttempt = try {
            Coordinate.fromString(value)
        } catch (_: Exception) {
            null
        }
        return parsedAttempt ?: indoorDataService.indoorData.getCoordinateOf(value)
    }



}