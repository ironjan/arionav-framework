package de.ironjan.arionav.ionav.special_routing.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.special_routing.model.Room
import de.ironjan.arionav.ionav.special_routing.model.readers.RoomOsmReader
import org.slf4j.LoggerFactory


class RoomRepository {
    private val inMemoryCache = mutableMapOf<String, MutableLiveData<Map<String, Room>>>()
    private val logger = LoggerFactory.getLogger(RoomRepository::class.java.simpleName)

    fun getRooms(osmFile: String): LiveData<Map<String, Room>> {
        var roomList = inMemoryCache[osmFile]

        if (roomList == null) {
            roomList = MutableLiveData()
            inMemoryCache[osmFile] = roomList
            RoomListAsyncLoadTask(roomList, osmFile).execute()

        }

        logger.info("Returning rooms.")
        return roomList
    }


    private class RoomListAsyncLoadTask(
        private val roomList: MutableLiveData<Map<String, Room>>,
        private val osmFile: String
    ) :
        AsyncTask<Void, Void, Map<String, Room>>() {
        private val logger = LoggerFactory.getLogger(RoomListAsyncLoadTask::class.java.simpleName)

        override fun doInBackground(vararg args: Void?): Map<String, Room> {
            logger.info("Starting to load...")
            val loaded =
                RoomOsmReader()
                    .parseOsmFile(osmFile)
                    .map { Pair(it.name, it) }
                    .toMap()

            logger.info("Loading complete.")
            return loaded
        }


        override fun onPostExecute(data: Map<String, Room>) {
            roomList.value = data
            logger.info("Updated live data with loaded rooms.")
        }
    }
}