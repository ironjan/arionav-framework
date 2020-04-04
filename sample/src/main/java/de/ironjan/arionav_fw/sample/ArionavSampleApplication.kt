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
    override val ionavContainer = IonavContainer(this, "uni_paderborn", R.raw.uni_paderborn)

    val sampleAppContainer = SampleAppContainer()

    override fun onCreate() {
        super.onCreate()

        setupLogging()

        GhzExtractor().unzipGhzToStorage(this, ionavContainer)

        // try to pre-fill places
        // todo useful?
        sampleAppContainer.namedPlaceRepository.getPlaces(ionavContainer.osmFilePath)

        ionavContainer.routingService.init(ionavContainer.mapFolder)
    }


    private fun setupLogging() {
        HandroidLoggerAdapter.APP_NAME = resources.getString(R.string.app_name)
    }

}