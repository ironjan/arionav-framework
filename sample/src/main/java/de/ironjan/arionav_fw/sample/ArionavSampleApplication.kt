package de.ironjan.arionav_fw.sample

import android.app.Application
import de.ironjan.arionav_fw.ionav.GhzExtractor
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import de.ironjan.arionav_fw.ionav.routing.repository.NamedPlaceRepository
import org.slf4j.impl.HandroidLoggerAdapter

class ArionavSampleApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        setupLogging()
        val ghzExtractor = GhzExtractor(this, ghzResId, mapName)
        ghzExtractor.unzipGhzToStorage()

        // try to pre-fill places
        NamedPlaceRepository.instance.getPlaces(ghzExtractor.osmFilePath)

        RoutingService.init(ghzExtractor)
    }


    private fun setupLogging() {
        HandroidLoggerAdapter.APP_NAME = resources.getString(R.string.app_name)
    }

    companion object{
        const val ghzResId = R.raw.uni_paderborn
        const val mapName = "uni_paderborn"
    }
}