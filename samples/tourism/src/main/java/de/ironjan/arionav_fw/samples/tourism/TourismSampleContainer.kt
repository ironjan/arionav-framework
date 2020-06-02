package de.ironjan.arionav_fw.samples.tourism

import android.content.Context
import de.ironjan.arionav_fw.ionav.di.IonavContainer
import de.ironjan.arionav_fw.samples.tourism.services.TourismDestinationService

class TourismSampleContainer(context: Context) : IonavContainer(context, "paderborn", R.raw.paderborn) {

    override fun init() {
        super.init()
        destinationService = TourismDestinationService()
            .apply {
                init(osmFilePath)
            }
    }
}