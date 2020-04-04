package de.ironjan.arionav_fw.ionav

import de.ironjan.arionav_fw.ionav.navigation.NavigationService
import de.ironjan.arionav_fw.ionav.positioning.PositioningService
import de.ironjan.arionav_fw.ionav.routing.RoutingService

/**
 * A container to implement manual dependency injection for ionav-components.
 *
 * @see <a href="https://developer.android.com/training/dependency-injection/manual">https://developer.android.com/training/dependency-injection/manual</a>
 */
class IonavContainer {
    val positioningService = PositioningService()
    val routingService = RoutingService()
    val navigationService = NavigationService(positioningService, routingService)
}