package de.ironjan.arionav.sample

import android.app.Application
import de.ironjan.arionav.ionav.GhzExtractor
import org.slf4j.impl.HandroidLoggerAdapter

class ArionavSampleApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        setupLogging()
        GhzExtractor(this, ghzResId, mapName).unzipGhzToStorage()
    }


    private fun setupLogging() {
        HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG
        HandroidLoggerAdapter.APP_NAME = resources.getString(R.string.app_name)

    }

    companion object{
        const val ghzResId = R.raw.uni_paderborn
        const val mapName = "uni_paderborn"
    }
}