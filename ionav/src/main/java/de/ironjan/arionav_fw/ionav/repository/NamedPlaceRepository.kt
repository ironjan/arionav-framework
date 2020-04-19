package de.ironjan.arionav_fw.ionav.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav_fw.ionav.model.NamedPlace
import de.ironjan.arionav_fw.ionav.model.readers.ImprovedPoiConverter
import de.ironjan.arionav_fw.ionav.model.readers.ImprovedRoomConverter
import de.ironjan.arionav_fw.ionav.model.readers.OsmConverter
import org.slf4j.LoggerFactory

class NamedPlaceRepository(private val osmFile: String) {
    private val roomConverter = ImprovedRoomConverter()
    private val poiConverter=  ImprovedPoiConverter()

    private var inMemoryCache: MutableLiveData<Map<String, NamedPlace>>? = null


    fun getPlaces(): LiveData<Map<String, NamedPlace>> {
        var places = inMemoryCache

        if (places == null) {
            places = MutableLiveData(mutableMapOf())
            inMemoryCache = places

            NamedPlacesAsyncLoadTask(places, osmFile, poiConverter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            NamedPlacesAsyncLoadTask(places, osmFile, roomConverter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
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
            val mutableMapOfPlaces = places.value?.toMutableMap() ?: mutableMapOf()

            result.forEach { mutableMapOfPlaces[it.key] = it.value }

            places.value = mutableMapOfPlaces

            logger.info("Updated live data with loaded POIs.")
        }
    }

}