package de.ironjan.arionav.ionav.room_routing

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.room_routing.model.Room
import org.slf4j.LoggerFactory


class RoomRepository {
    private val inMemoryCache = mutableMapOf<String, MutableLiveData<List<Room>>>()
    private val logger = LoggerFactory.getLogger("RoomRepository")

    fun getRooms(osmFile: String): LiveData<List<Room>> {
        var roomList = inMemoryCache[osmFile]

        if (roomList == null) {
            roomList = MutableLiveData()
            inMemoryCache[osmFile] = roomList
            logger.info("Starting to load...")
            val loaded = loadFromDisk(osmFile)
            roomList.value = loaded

            logger.info("Loading complete.")
        }

        logger.info("Returning rooms.")
        return roomList
    }

    private fun loadFromDisk(osmFile: String): List<Room> {
        return RoomOsmReader().parseOsmFile(osmFile)
    }
}