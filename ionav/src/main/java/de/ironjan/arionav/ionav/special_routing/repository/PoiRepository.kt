package de.ironjan.arionav.ionav.special_routing.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.special_routing.model.Poi
import de.ironjan.arionav.ionav.special_routing.model.readers.PoiOsmReader
import org.slf4j.LoggerFactory
import kotlin.math.log

class PoiRepository {
    private val inMemoryCache = mutableMapOf<String, MutableLiveData<Map<String,Poi>>>()
    private val logger = LoggerFactory.getLogger(PoiRepository::class.java.simpleName)

    fun getPois(osmFile: String) : LiveData<Map<String,Poi>> {
        var pois = inMemoryCache[osmFile]

        if (pois == null) {
            pois = MutableLiveData()
            inMemoryCache[osmFile] = pois
            PoiListAsyncLoadTask(pois, osmFile).execute()
        }
        return pois
    }
    /*
      private val inMemoryCache = mutableMapOf<String, MutableLiveData<List<Room>>>()
    private val logger = LoggerFactory.getLogger("RoomRepository")

    fun getRooms(osmFile: String): LiveData<List<Room>> {
        var roomList = inMemoryCache[osmFile]

        if (roomList == null) {
            roomList = MutableLiveData()
            inMemoryCache[osmFile] = roomList
            RoomListAsyncLoadTask(roomList, osmFile).execute()

        }

        logger.info("Returning rooms.")
        return roomList
    }


     */

    private class PoiListAsyncLoadTask(private val poiList: MutableLiveData<Map<String,Poi>>,
                                       private val osmFile: String)
        : AsyncTask<Void, Void, Map<String,Poi>>() {
        private val logger = LoggerFactory.getLogger(PoiListAsyncLoadTask::class.java.simpleName)

        override fun doInBackground(vararg p0: Void?): Map<String,Poi> {
            logger.info("Starting to load POIs...")
            val loaded =
                PoiOsmReader()
                    .parseOsmFile(osmFile)
                    .map { Pair(it.name, it) }
                    .toMap()

            logger.info("Loading complete.")

            return loaded
        }

        override fun onPostExecute(result: Map<String,Poi>?) {
            poiList.value = result
            logger.info("Updated live data with loaded POIs.")
        }
    }
}