package de.ironjan.arionav.ionav.room_routing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.room_routing.model.Room
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

class RoomRepository {
    private val inMemoryCache = mutableMapOf<String, MutableLiveData<List<Room>>>()
    private val logger = LoggerFactory.getLogger("RoomRepository")

    fun getRooms(osmFile: String): LiveData<List<Room>> {
        val roomList = inMemoryCache[osmFile] ?: MutableLiveData()
        inMemoryCache[osmFile] = roomList

        MainScope().launch {
            logger.info("Triggered background load for rooms.")
            roomList.value = loadFromDisk(osmFile)
            logger.info("Completed background load for rooms. Updated rooms live data.")
        }

        logger.info("Returning rooms.")
        return roomList
    }

    private suspend fun loadFromDisk(osmFile: String): List<Room> {
        return RoomOsmReader().parseOsmFile(osmFile)
    }
}