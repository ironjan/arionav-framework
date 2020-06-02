package de.ironjan.arionav_fw.ionav.di

import android.content.Context
import de.ironjan.arionav_fw.ionav.services.*
import java.io.File

/**
 * A container to implement manual dependency injection for ionav-components.
 *
 * @see <a href="https://developer.android.com/training/dependency-injection/manual">https://developer.android.com/training/dependency-injection/manual</a>
 */
open class IonavContainer(private val context: Context, val mapName: String, val resId: Int) {
    val applicationContext = context

    open val mapFolderPath: String by lazy { File(context.filesDir, mapName).absolutePath }
    open val mapFilePath: String by lazy { File(mapFolderPath, "$mapName.map").absolutePath }
    open val osmFilePath: String by lazy { File(mapFolderPath, "$mapName.osm").absolutePath }

    lateinit var indoorDataService: IndoorDataService
    lateinit var destinationService: IndoorDestinationService

    lateinit var  positioningService: PositioningService
    lateinit var  routingService: RoutingService
    lateinit var  navigationService: NavigationService

    open fun init() {
        indoorDataService = IndoorDataService()
        destinationService = IndoorDestinationService(indoorDataService)

        positioningService = PositioningService()
        routingService = RoutingService()
        navigationService = NavigationService(positioningService, routingService)

        routingService.init(mapFolderPath)
        indoorDataService.init(osmFilePath)
    }
}