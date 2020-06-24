package de.ironjan.arionav_fw.ionav.services

import android.os.AsyncTask
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.model.readers.IndoorMapDataLoadingTask
import de.ironjan.arionav_fw.ionav.util.Observable
import de.ironjan.arionav_fw.ionav.util.Observer
import org.slf4j.LoggerFactory

class IndoorDataService : Observable<IndoorDataState> {

    private val logger = LoggerFactory.getLogger(IndoorDataService::class.simpleName)

    override var state = IndoorDataState(IndoorData.empty(), IndoorDataLoadingState.INITIALIZED)
        private set(value) {
            field = value
            notifyObservers()
        }

    val indoorData: IndoorData
        get() = state.indoorData


    val loadingState
        get() = state.indoorDataLoadingState


    fun init(osmFilePath: String) {
        synchronized(loadingState) {
            if (loadingState == IndoorDataLoadingState.INITIALIZED) {
                IndoorMapDataLoadingTask(osmFilePath, this::onLoadComplete).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                state = state.copy(indoorDataLoadingState = IndoorDataLoadingState.LOADING)
            }
        }

        logger.info("Started loading of indoor map data.")
    }

    fun onLoadComplete(loadedData:IndoorData) {
        state = IndoorDataState(loadedData, IndoorDataLoadingState.READY)
        logger.info("Completed loading of indoor map data.")
    }


    // region observable
    private val _observers = mutableListOf<Observer<IndoorDataState>>()

    override fun registerObserver(observer: Observer<IndoorDataState>) {
        if (_observers.contains(observer)) return
        _observers.add(observer)
    }

    override fun removeObserver(observer: Observer<IndoorDataState>) {
        _observers.remove(observer)
    }

    override fun notifyObservers() {
        logger.debug("PositioningService notifying observers.")

        _observers.toList().forEach { it.update(state) }
    }
    // endregion
}