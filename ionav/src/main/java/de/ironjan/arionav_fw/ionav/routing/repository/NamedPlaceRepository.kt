package de.ironjan.arionav_fw.ionav.routing.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav_fw.ionav.routing.model.NamedPlace
import de.ironjan.arionav_fw.ionav.routing.model.readers.ImprovedPoiConverter
import de.ironjan.arionav_fw.ionav.routing.model.readers.ImprovedRoomConverter
import de.ironjan.arionav_fw.ionav.routing.model.readers.OsmConverter
import org.slf4j.LoggerFactory

class NamedPlaceRepository(
    val roomReader: ImprovedRoomConverter,
    val poiReader: ImprovedPoiConverter
) {
    private val inMemoryCache = mutableMapOf<String, MutableLiveData<Map<String, NamedPlace>>>()
    private val logger = LoggerFactory.getLogger(NamedPlaceRepository::class.java.simpleName)


    fun getPlaces(osmFile: String): LiveData<Map<String, NamedPlace>> {
        var places = inMemoryCache[osmFile]

        if (places == null) {
            places = MutableLiveData()
            inMemoryCache[osmFile] = places

            NamedPlacesAsyncLoadTask(places, osmFile, poiReader).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            NamedPlacesAsyncLoadTask(places, osmFile, roomReader).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        }
        return places
    }

    private class NamedPlacesAsyncLoadTask(
        private val places: MutableLiveData<Map<String, NamedPlace>>,
        private val osmFile: String,
        val converter: OsmConverter<NamedPlace>
    ) : AsyncTask<Void, Void, Map<String, NamedPlace>>() {
        private val logger = LoggerFactory.getLogger(NamedPlacesAsyncLoadTask::class.java.simpleName)

        override fun doInBackground(vararg p0: Void?): Map<String, NamedPlace> {
            logger.info("Starting to load places with $converter... (OsmReader)")
            val start = System.currentTimeMillis()

            val loaded =
                converter
                    .parseOsmFile(osmFile)
                    .map { Pair(it.name, it) }
                    .toMap()

            val duration =System.currentTimeMillis() - start
            logger.info("Loading complete after ${duration}ms... $converter... (OsmReader)")

            return loaded
        }

        override fun onPostExecute(result: Map<String, NamedPlace>) {
            val mutableDate = places.value?.toMutableMap() ?: mutableMapOf()
            result.forEach { mutableDate[it.key] = it.value }
            places.value = mutableDate
            logger.info("Updated live data with loaded POIs.")
        }
    }

    companion object {
        val instance: NamedPlaceRepository = NamedPlaceRepository(ImprovedRoomConverter(), ImprovedPoiConverter())
    }
}