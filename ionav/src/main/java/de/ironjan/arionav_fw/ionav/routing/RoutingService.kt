package de.ironjan.arionav_fw.ionav.routing

import android.os.AsyncTask
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.LoadGraphTask
import de.ironjan.graphhopper.extensions_core.Coordinate
import de.ironjan.graphhopper.levelextension.Routing
import org.slf4j.LoggerFactory

class RoutingService {
    private val logger = LoggerFactory.getLogger(RoutingService::class.java.simpleName)

    private var routing: Routing = UninitializedRouting()

    var initialized = false
        private set

    fun route(from: Coordinate?, to: Coordinate?): PathWrapper?
            = routing.route(from, to)

    fun route(fromLat: Double, fromLon: Double, fromLvl: Double, toLat: Double, toLon: Double, toLvl: Double): PathWrapper?
            =  routing.route(fromLat, fromLon, fromLvl, toLat, toLon, toLvl)

    fun init(mapFolder: String) {
        val loadGraphTask = LoadGraphTask(mapFolder, object : LoadGraphTask.Callback {
            override fun onSuccess(graphHopper: GraphHopper) {
                logger.debug("Completed loading graph.")

                routing = Routing(graphHopper)
                initialized = true
            }

            override fun onError(exception: Exception) {
                logger.error("Error when loading graph: $exception")
                // FIXME show error
            }

        })
        loadGraphTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    /** Used as routing backend as long as the service is not initialized. Returns null-routes. */
    class UninitializedRouting : Routing(null) {
        override fun route(from: Coordinate?, to: Coordinate?): PathWrapper? {
            return null
        }

        override fun route(fromLat: Double, fromLon: Double, fromLvl: Double, toLat: Double, toLon: Double, toLvl: Double): PathWrapper? {
            return null
        }
    }

}