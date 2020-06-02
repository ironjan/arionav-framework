package de.ironjan.arionav_fw.samples.tourism.services

import android.os.AsyncTask
import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.services.DestinationService
import de.ironjan.arionav_fw.ionav.services.DestinationServiceState
import de.ironjan.graphhopper.extensions_core.Coordinate
import java.lang.Exception

class TourismDestinationService : DestinationService() {
    private var destinationNodes = mapOf<String, Node>()

    override val state: DestinationServiceState
        get() = TourismDestinationServiceState(destinations, destinationNodes)

    override fun getCoordinate(value: String): Coordinate? = try{
        Coordinate.fromString(value)
    } catch (e: Exception) {
        destinations[value]
    }

    fun init(osmFilePath: String) {
        val loadingTask = TourismDataLoadingTask(osmFilePath) {
            destinationNodes = it
            destinations = it.map { entry -> Pair(entry.key, Coordinate(entry.value.lat,entry.value.lon, 0.0)) }.toMap()
        }
        loadingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }


}
