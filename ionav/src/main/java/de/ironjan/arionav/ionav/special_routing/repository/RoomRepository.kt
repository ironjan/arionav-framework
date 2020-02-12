package de.ironjan.arionav.ionav.special_routing.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.special_routing.model.Room
import de.ironjan.arionav.ionav.special_routing.model.readers.RoomOsmReader
import org.slf4j.LoggerFactory


class RoomRepository {
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


    private class RoomListAsyncLoadTask(private val roomList: MutableLiveData<List<Room>>,
                                        private val osmFile: String):
        AsyncTask<String, Void, List<Room>>() {
        private val logger = LoggerFactory.getLogger(RoomListAsyncLoadTask::class.java.simpleName)

        override fun doInBackground(vararg args: String): List<Room> {
            logger.info("Starting to load...")
            val loaded = RoomOsmReader().parseOsmFile(osmFile)

            logger.info("Loading complete.")
            return loaded
        }


        override fun onPostExecute(data: List<Room>) {
            roomList.value = data
            logger.info("Loading completed and post execute done.")
        }
    }
}