package de.ironjan.arionav.sample

import android.app.Application
import de.ironjan.arionav.ionav.GhzExtractor
import org.slf4j.impl.HandroidLoggerAdapter

class ArionavSampleApplication : Application() {
    private val ghzResId = R.raw.saw
    private val mapName = "saw"

    override fun onCreate() {
        super.onCreate()
        setupLogging()
        GhzExtractor(this, ghzResId, mapName).unzipGhzToStorage()
    }


    private fun setupLogging() {
        HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG;
        HandroidLoggerAdapter.APP_NAME = resources.getString(R.string.app_name);

    }

}