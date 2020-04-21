package de.ironjan.arionav_fw.ionav

import android.content.Context
import de.ironjan.arionav_fw.ionav.navigation.NavigationService
import de.ironjan.arionav_fw.ionav.positioning.PositioningService
import de.ironjan.arionav_fw.ionav.repository.NamedPlaceRepository
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import java.io.File

/**
 * A container to implement manual dependency injection for ionav-components.
 *
 * @see <a href="https://developer.android.com/training/dependency-injection/manual">https://developer.android.com/training/dependency-injection/manual</a>
 */
class IonavContainer(private val context: Context, val mapName: String, val resId: Int) {
    val applicationContext = context

    val mapFolder by lazy { File(context.filesDir, mapName).absolutePath }
    val mapFilePath by lazy { File(mapFolder, "$mapName.map").absolutePath }
    val osmFilePath by lazy { File(mapFolder, "$mapName.osm").absolutePath }

    val positioningService = PositioningService()
    val routingService = RoutingService()
    val navigationService = NavigationService(positioningService, routingService)


    val namedPlaceRepository by lazy {  NamedPlaceRepository(osmFilePath) }
}