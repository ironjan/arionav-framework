package de.ironjan.arionav_fw.ionav.navigation

import de.ironjan.arionav_fw.ionav.positioning.IPositionObserver
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.positioning.PositioningService
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import org.slf4j.LoggerFactory

class NavigationService(
    positioningService: PositioningService,
    private val routingService: RoutingService
) {
    private val logger = LoggerFactory.getLogger(NavigationService::class.java.simpleName)

    val positionObserver = object : IPositionObserver {
        override fun onPositionChange(c: IonavLocation?) {
            logger.info("$TAG received position change.")
        }

    }

    init {
        positioningService.registerObserver(positionObserver)
    }

    companion object {
        const val TAG = "NavigationService"
    }
}