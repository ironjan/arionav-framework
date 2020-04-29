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

class RoutingService : Observable<RoutingServiceState> {
    // region observer handling
    private val _observers = mutableListOf<Observer<RoutingServiceState>>()

    override fun registerObserver(observer: Observer<RoutingServiceState>) {
        if (_observers.contains(observer)) return
        _observers.add(observer)
    }

    override fun removeObserver(observer: Observer<RoutingServiceState>) {
        _observers.remove(observer)
    }

    override fun notifyObservers() {
        _observers.map { it.update(state) }
    }
    // endregion

    // region state
    override var state = RoutingServiceState(Status.UNINITIALIZED)
        private set(value) {
            field = value
            notifyObservers()
        }

    val status
        get() = state.status

    val initialized
        get() = status == Status.READY


    enum class Status {
        UNINITIALIZED, LOADING, READY, ERROR
    }
    // endregion


    private val logger = LoggerFactory.getLogger(RoutingService::class.java.simpleName)

    // region routing and API wrapping
    private var routing: Routing = UninitializedRouting()



    fun route(from: Coordinate, to: Coordinate): PathWrapper? = try {
        routing.route(from, to)
    } catch (e: java.lang.Exception) {
        logger.error("Could not compute route.", e)
        null
    } catch (e: StackOverflowError) {
        logger.error("Could not compute route.", e)
        null
    }

    fun init(mapFolder: String) {
        state = state.copy(status = Status.LOADING)

        val loadGraphTask = LoadGraphTask(mapFolder, object : LoadGraphTask.Callback {
            override fun onSuccess(graphHopper: GraphHopper) {
                logger.debug("Completed loading graph.")

                routing = Routing(graphHopper)
                state = state.copy(status = Status.READY)
            }

            override fun onError(exception: Exception) {
                logger.error("Error when loading graph: $exception")
                // FIXME show error
                state = state.copy(status = Status.ERROR)
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
    // endregion


}