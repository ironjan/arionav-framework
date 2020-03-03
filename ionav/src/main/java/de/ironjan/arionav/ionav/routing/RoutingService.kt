package de.ironjan.arionav.ionav.routing

import android.os.AsyncTask
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import de.ironjan.arionav.ionav.GhzExtractor
import de.ironjan.arionav.ionav.LoadGraphTask
import de.ironjan.graphhopper.extensions_core.Coordinate
import de.ironjan.graphhopper.levelextension.Routing
import org.slf4j.LoggerFactory

class RoutingService private constructor() {
    private val logger = LoggerFactory.getLogger(RoutingService::class.java.simpleName)

    private var routing: Routing = UninitializedRouting()
    private var initialized = false

    fun init(ghzExtractor: GhzExtractor) {
        val loadGraphTask = LoadGraphTask(ghzExtractor.mapFolder, object : LoadGraphTask.Callback {
            override fun onSuccess(hopper: GraphHopper) {
                logger.debug("Completed loading graph.")

                routing = Routing(hopper)
                initialized = true
            }

            override fun onError(exception: Exception) {
                logger.error("Error when loading graph: $exception")
                // FIXME show error
            }

        })
        loadGraphTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    companion object {
        private val Instance = RoutingService()

        val initialized: Boolean
            get() = Instance.initialized

        fun init(ghzExtractor: GhzExtractor) = Instance.init(ghzExtractor)
        fun route(from: Coordinate, to: Coordinate): PathWrapper? = Instance.routing.route(from, to)
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