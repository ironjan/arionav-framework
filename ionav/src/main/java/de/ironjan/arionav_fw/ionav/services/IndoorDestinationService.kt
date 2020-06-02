package de.ironjan.arionav_fw.ionav.services

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.model.readers.IndoorMapDataLoadingTask
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

class IndoorDestinationService(private val indoorDataService: IndoorDataService) {

    private val _indoorData = MutableLiveData<IndoorData>(IndoorData.empty())
    val indoorData: LiveData<IndoorData> = _indoorData

    private val _isIndoorDataLoaded: Boolean
        get() = _loadingState == IndoorDataLoadingState.LOADED

    private var _loadingState = IndoorDataLoadingState.UNLOADED

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
        return parsedAttempt ?: indoorDataService.indoorData.value?.getCoordinateOf(value)
    }

    enum class IndoorDataLoadingState {
        UNLOADED, LOADING, LOADED
    }
}