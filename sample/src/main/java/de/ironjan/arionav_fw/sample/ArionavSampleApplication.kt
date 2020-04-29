package de.ironjan.arionav_fw.sample

import android.app.Application
import de.ironjan.arionav_fw.ionav.GhzExtractor
import de.ironjan.arionav_fw.ionav.di.IonavContainer
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import org.slf4j.impl.HandroidLoggerAdapter

class ArionavSampleApplication : Application(), IonavContainerHolder {

    /**
     * The ionav-components dependency container. Can be accessed via: <code>(application as IonavContainerHolder).ionavContainer</code>.
     */
    override val ionavContainer = IonavContainer(this, "uni_paderborn", R.raw.uni_paderborn)


    override fun onCreate() {
        super.onCreate()

        setupLogging()

        GhzExtractor().unzipGhzToStorage(this, ionavContainer)


        ionavContainer.routingService.init(ionavContainer.mapFolderPath)
    }


    private fun setupLogging() {
        HandroidLoggerAdapter.APP_NAME = resources.getString(R.string.app_name)
    }

}