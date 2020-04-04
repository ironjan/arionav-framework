package de.ironjan.arionav_fw.sample

import android.app.Application
import de.ironjan.arionav_fw.ionav.GhzExtractor
import de.ironjan.arionav_fw.ionav.IonavContainer
import de.ironjan.arionav_fw.ionav.IonavContainerHolder
import org.slf4j.impl.HandroidLoggerAdapter

class ArionavSampleApplication : Application(), IonavContainerHolder {

    /**
     * The ionav-components dependency container. Can be accessed via: <code>(application as IonavContainerHolder).ionavContainer</code>.
     */
    override val ionavContainer = IonavContainer(this, mapName, ghzResId)

    val sampleAppContainer = SampleAppContainer()

    override fun onCreate() {
        super.onCreate()
        Instance = this

        setupLogging()

        GhzExtractor().unzipGhzToStorage(this, ionavContainer)

        // try to pre-fill places
        sampleAppContainer.namedPlaceRepository.getPlaces(ionavContainer.osmFilePath)

        ionavContainer.routingService.init(ionavContainer.mapFolder)
    }


    private fun setupLogging() {
        HandroidLoggerAdapter.APP_NAME = resources.getString(R.string.app_name)
    }

    companion object{
        const val ghzResId = R.raw.uni_paderborn
        const val mapName = "uni_paderborn"

        var Instance : ArionavSampleApplication? = null
    }
}