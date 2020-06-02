package de.ironjan.arionav_fw.samples.tourism.services

import android.os.AsyncTask
import de.ironjan.arionav_fw.ionav.services.DestinationService
import de.ironjan.arionav_fw.ionav.services.DestinationServiceState
import de.ironjan.graphhopper.extensions_core.Coordinate
import java.lang.Exception

class TourismDestinationService : DestinationService() {
    override val state: DestinationServiceState
        get() = DestinationServiceState(destinations)

    override fun getCoordinate(value: String): Coordinate? = try{
        Coordinate.fromString(value)
    } catch (e: Exception) {
        destinations[value]
    }

    fun init(osmFilePath: String) {
        val loadingTask = LoadingTask(osmFilePath) { destinations = it }
        loadingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }


}
