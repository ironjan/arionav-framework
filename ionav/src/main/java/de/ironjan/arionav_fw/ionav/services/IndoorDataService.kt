package de.ironjan.arionav_fw.ionav.services

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.model.readers.IndoorMapDataLoadingTask
import org.slf4j.LoggerFactory

class IndoorDataService {
    private val logger = LoggerFactory.getLogger(IndoorDataService::class.simpleName)

    private val _indoorData = MutableLiveData<IndoorData>(IndoorData.empty())
    val indoorData: LiveData<IndoorData> = _indoorData

    private val _isIndoorDataLoaded: Boolean
        get() = _loadingState == IndoorDestinationService.IndoorDataLoadingState.LOADED

    private var _loadingState = IndoorDestinationService.IndoorDataLoadingState.UNLOADED

    fun init(osmFilePath: String) {
        val callback = { loadedData: IndoorData ->
            _indoorData.value = loadedData
            _loadingState = IndoorDestinationService.IndoorDataLoadingState.LOADED
            logger.info("Completed loading of indoor map data.")
        }

        synchronized(_loadingState) {
            if (_loadingState == IndoorDestinationService.IndoorDataLoadingState.UNLOADED) {
                IndoorMapDataLoadingTask(osmFilePath, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                _loadingState = IndoorDestinationService.IndoorDataLoadingState.LOADING
            }
        }

        logger.info("Started loading of indoor map data.")
    }
}