package de.ironjan.arionav_fw.ionav.di

import android.content.Context
import de.ironjan.arionav_fw.ionav.services.NavigationService
import de.ironjan.arionav_fw.ionav.services.PositioningService
import de.ironjan.arionav_fw.ionav.services.RoutingService
import java.io.File

/**
 * A container to implement manual dependency injection for ionav-components.
 *
 * @see <a href="https://developer.android.com/training/dependency-injection/manual">https://developer.android.com/training/dependency-injection/manual</a>
 */
class IonavContainer(private val context: Context, val mapName: String, val resId: Int) {
    val applicationContext = context

    val mapFolderPath: String by lazy { File(context.filesDir, mapName).absolutePath }
    val mapFilePath: String by lazy { File(mapFolderPath, "$mapName.map").absolutePath }
    val osmFilePath: String by lazy { File(mapFolderPath, "$mapName.osm").absolutePath }

    val positioningService = PositioningService()
    val routingService = RoutingService()
    val navigationService = NavigationService(positioningService, routingService)

}