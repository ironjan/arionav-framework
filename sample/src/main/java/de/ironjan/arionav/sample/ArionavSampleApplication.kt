package de.ironjan.arionav.sample

import android.app.Application
import de.ironjan.arionav.ionav.GhzExtractor
import de.ironjan.arionav.ionav.special_routing.repository.NamedPlaceRepository
import org.slf4j.impl.HandroidLoggerAdapter

class ArionavSampleApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        setupLogging()
        val ghzExtractor = GhzExtractor(this, ghzResId, mapName)
        ghzExtractor.unzipGhzToStorage()

        // try to pre-fill places
        NamedPlaceRepository.instance.getPlaces(ghzExtractor.osmFilePath)
    }


    private fun setupLogging() {
        HandroidLoggerAdapter.APP_NAME = resources.getString(R.string.app_name)
    }

    companion object{
        const val ghzResId = R.raw.uni_paderborn
        const val mapName = "uni_paderborn"
    }
}