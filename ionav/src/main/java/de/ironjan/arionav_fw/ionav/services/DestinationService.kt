package de.ironjan.arionav_fw.ionav.services

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.model.readers.IndoorMapDataLoadingTask
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

class DestinationService() {
    private val logger = LoggerFactory.getLogger(DestinationService::class.simpleName)

    private val _indoorData = MutableLiveData<IndoorData>(IndoorData.empty())
    val indoorData: LiveData<IndoorData> = _indoorData

    private val _isIndoorDataLoaded: Boolean
        get() = _loadingState == IndoorDataLoadingState.LOADED

    private var _loadingState = IndoorDataLoadingState.UNLOADED

    fun init(osmFilePath: String) {
        val callback = { loadedData: IndoorData ->
            _indoorData.value = loadedData
            _loadingState = IndoorDataLoadingState.LOADED
            logger.info("Completed loading of indoor map data.")
        }

        synchronized(_loadingState) {
            if (_loadingState == IndoorDataLoadingState.UNLOADED) {
                IndoorMapDataLoadingTask(osmFilePath, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                _loadingState = IndoorDataLoadingState.LOADING
            }
        }

        logger.info("Started loading of indoor map data.")
    }

    fun getCoordinate(value: String): Coordinate? {
        val parsedAttempt = try {
            Coordinate.fromString(value)
        } catch (_: Exception) {
            null
        }
        return parsedAttempt ?: _indoorData.value?.getCoordinateOf(value)
    }

    enum class IndoorDataLoadingState {
        UNLOADED, LOADING, LOADED
    }
}