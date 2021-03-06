package de.ironjan.arionav_fw.ionav.di

import android.content.Context
import de.ironjan.arionav_fw.ionav.services.*
import java.io.File

/**
 * A container to implement manual dependency injection for ionav-components.
 *
 * @see <a href="https://developer.android.com/training/dependency-injection/manual">https://developer.android.com/training/dependency-injection/manual</a>
 */
open class IonavContainer(private val context: Context, val mapName: String, val resId: Int, val developerMails: Array<String>) {

    val applicationContext = context

    open val mapFolderPath: String by lazy { File(context.filesDir, mapName).absolutePath }
    open val mapFilePath: String by lazy { File(mapFolderPath, "$mapName.map").absolutePath }
    open val osmFilePath: String by lazy { File(mapFolderPath, "$mapName.osm").absolutePath }

    val indoorDataService = IndoorDataService()
    var destinationService: DestinationService = IndoorDestinationService(indoorDataService)
        protected set

    val positioningService = PositioningService()
    val routingService = RoutingService()
    val navigationService = NavigationService(positioningService, routingService)

    val instructionHelper = InstructionHelper(context)

    open fun init() {
        routingService.init(mapFolderPath)
        indoorDataService.init(osmFilePath)
    }
}