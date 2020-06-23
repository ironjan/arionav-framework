package de.ironjan.arionav_fw.samples.tourism

import android.content.Context
import de.ironjan.arionav_fw.ionav.di.IonavContainer
import de.ironjan.arionav_fw.samples.tourism.services.TourismDestinationService

class TourismSampleContainer(context: Context) : IonavContainer(context, "paderborn", R.raw.paderborn, arrayOf("irb-git+mtljan-thesis-6522-24oauqa8lahux188otoxb4l1i-issue@mail.uni-paderborn.de")) {

    override fun init() {
        super.init()
        destinationService = TourismDestinationService()
            .apply {
                init(osmFilePath)
            }
    }
}