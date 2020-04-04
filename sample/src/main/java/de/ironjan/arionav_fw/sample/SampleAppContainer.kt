package de.ironjan.arionav_fw.sample

import de.ironjan.arionav_fw.ionav.GhzExtractor
import de.ironjan.arionav_fw.ionav.routing.model.readers.ImprovedPoiReader
import de.ironjan.arionav_fw.ionav.routing.model.readers.ImprovedRoomReader
import de.ironjan.arionav_fw.ionav.routing.repository.NamedPlaceRepository

class SampleAppContainer {


    private val roomReader = ImprovedRoomReader()
    private val poiReader = ImprovedPoiReader()

    val namedPlaceRepository = NamedPlaceRepository(roomReader, poiReader)

    companion object {
        const val ghzResId = R.raw.uni_paderborn
        const val mapName = "uni_paderborn"
    }
}