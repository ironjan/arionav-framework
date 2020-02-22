package de.ironjan.arionav.ionav.special_routing.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.special_routing.model.NamedPlace
import de.ironjan.arionav.ionav.special_routing.model.readers.OsmReader
import de.ironjan.arionav.ionav.special_routing.model.readers.PoiOsmReader
import de.ironjan.arionav.ionav.special_routing.model.readers.RoomOsmReader
import org.slf4j.LoggerFactory

class NamedPlaceRepository {
    private val inMemoryCache = mutableMapOf<String, MutableLiveData<Map<String, NamedPlace>>>()
    private val logger = LoggerFactory.getLogger(NamedPlaceRepository::class.java.simpleName)

    private val roomReader: OsmReader<NamedPlace> = OsmReader(
        RoomOsmReader.isNamedRoomFilter,
        RoomOsmReader.allNodeFilter,
        RoomOsmReader.osmToRoomConverter
    )
    private val poiReader: OsmReader<NamedPlace> = OsmReader(
        PoiOsmReader.noWaysFilter,
        PoiOsmReader.poiNodeFilter,
        PoiOsmReader.converter
    )

    fun getPlaces(osmFile: String): LiveData<Map<String, NamedPlace>> {
        var places = inMemoryCache[osmFile]

        if (places == null) {
            places = MutableLiveData()
            inMemoryCache[osmFile] = places

            NamedPlacesAsyncLoadTask(places, osmFile, poiReader).execute()
            NamedPlacesAsyncLoadTask(places, osmFile, roomReader).execute()

        }
        return places
    }

    private class NamedPlacesAsyncLoadTask(
        private val places: MutableLiveData<Map<String, NamedPlace>>,
        private val osmFile: String,
        val reader: OsmReader<NamedPlace>
    ) : AsyncTask<Void, Void, Map<String, NamedPlace>>() {
        private val logger = LoggerFactory.getLogger(NamedPlacesAsyncLoadTask::class.java.simpleName)

        override fun doInBackground(vararg p0: Void?): Map<String, NamedPlace> {
            logger.info("Starting to load places with $reader...")
            val loaded =
                reader
                    .parseOsmFile(osmFile)
                    .map { Pair(it.name, it) }
                    .toMap()

            logger.info("Loading complete.")

            return loaded
        }

        override fun onPostExecute(result: Map<String, NamedPlace>) {
            val mutableDate = places.value?.toMutableMap() ?: mutableMapOf()
            result.forEach { mutableDate[it.key] = it.value }
            places.value = mutableDate
            logger.info("Updated live data with loaded POIs.")
        }
    }

    private class RoomListAsyncLoadTask(
        private val places: MutableLiveData<Map<String, NamedPlace>>,
        private val osmFile: String
    ) :
        AsyncTask<Void, Void, Map<String, NamedPlace>>() {
        private val logger = LoggerFactory.getLogger(RoomListAsyncLoadTask::class.java.simpleName)

        override fun doInBackground(vararg args: Void?): Map<String, NamedPlace> {
            logger.info("Starting to load...")
            val loaded =
                RoomOsmReader()
                    .parseOsmFile(osmFile)
                    .map { Pair(it.name, it) }
                    .toMap()

            logger.info("Loading complete.")
            return loaded
        }


        override fun onPostExecute(result: Map<String, NamedPlace>) {
            val mutableDate = places.value?.toMutableMap() ?: mutableMapOf()
            result.forEach { mutableDate[it.key] = it.value }
            places.value = mutableDate
            logger.info("Updated live data with loaded rooms.")
        }
    }
}