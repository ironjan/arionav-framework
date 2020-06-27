package de.ironjan.arionav_fw.samples.campus

import android.app.Application
import de.ironjan.arionav_fw.ionav.GhzExtractor
import de.ironjan.arionav_fw.ionav.di.IonavContainer
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import org.slf4j.impl.HandroidLoggerAdapter

class CampusSampleApplication : Application(), IonavContainerHolder {

    /**
     * The ionav-components dependency container. Can be accessed via: <code>(application as IonavContainerHolder).ionavContainer</code>.
     */
    override val ionavContainer = IonavContainer(this, "arionav_map", R.raw.arionav_map, arrayOf("irb-git+mtljan-thesis-6522-24oauqa8lahux188otoxb4l1i-issue@mail.uni-paderborn.de"))


    override fun onCreate() {
        super.onCreate()

        setupLogging()

        GhzExtractor().unzipGhzToStorage(this, ionavContainer)

        ionavContainer.init()
    }


    private fun setupLogging() {
        HandroidLoggerAdapter.APP_NAME = resources.getString(R.string.app_name)
    }

}