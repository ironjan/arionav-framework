package de.ironjan.arionav_fw.ionav.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.graphhopper.extensions_core.Coordinate

class IndoorDestinationService(private val indoorDataService: IndoorDataService) {

    private val _indoorData = MutableLiveData<IndoorData>(IndoorData.empty())
    val indoorData: LiveData<IndoorData> = _indoorData

    private val _isIndoorDataLoaded: Boolean
        get() = _loadingState == IndoorDataLoadingState.READY

    private var _loadingState = IndoorDataLoadingState.INITIALIZED

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