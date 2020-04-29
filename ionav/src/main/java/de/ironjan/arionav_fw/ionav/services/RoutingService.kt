package de.ironjan.arionav_fw.ionav.services

import android.os.AsyncTask
import com.graphhopper.GraphHopper
import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.util.LoadGraphTask
import de.ironjan.arionav_fw.ionav.util.Observable
import de.ironjan.arionav_fw.ionav.util.Observer
import de.ironjan.graphhopper.extensions_core.Coordinate
import de.ironjan.graphhopper.levelextension.Routing
import org.slf4j.LoggerFactory

class RoutingService : Observable<RoutingService.Status> {
    override fun registerObserver(observer: Observer<Status>) {
        if (_observers.contains(observer)) return
        _observers.add(observer)
    }

    override fun removeObserver(observer: Observer<Status>) {
        _observers.remove(observer)
    }

    override val state
        get() = status

    override fun notifyObservers() {
        _observers.map { it.update(state) }
    }

    private val _observers = mutableListOf<Observer<Status>>()

    private val logger = LoggerFactory.getLogger(RoutingService::class.java.simpleName)

    private var routing: Routing = UninitializedRouting()

    var status = Status.UNINITIALIZED
        private set(value) {
            field = value
            notifyObservers()
        }

    var initialized = false
        private set

    fun route(from: Coordinate?, to: Coordinate?): PathWrapper? = try {
        routing.route(from, to)
    } catch (e: java.lang.Exception) {
        logger.error("Could not compute route.", e)
        null
    } catch (e: StackOverflowError) {
        logger.error("Could not compute route.", e)
        null
    }

    fun init(mapFolder: String) {
        status = Status.LOADING
        val loadGraphTask = LoadGraphTask(mapFolder, object : LoadGraphTask.Callback {
            override fun onSuccess(graphHopper: GraphHopper) {
                logger.debug("Completed loading graph.")

                routing = Routing(graphHopper)
                initialized = true
                status = Status.READY
            }

            override fun onError(exception: Exception) {
                logger.error("Error when loading graph: $exception")
                // FIXME show error
                status = Status.ERROR
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

    enum class Status {
        UNINITIALIZED, LOADING, READY, ERROR
    }
}