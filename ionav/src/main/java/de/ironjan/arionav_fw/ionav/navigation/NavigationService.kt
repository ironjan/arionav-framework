package de.ironjan.arionav_fw.ionav.navigation

import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.positioning.IPositionObserver
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.positioning.PositioningService
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import de.ironjan.arionav_fw.ionav.util.Observable
import de.ironjan.arionav_fw.ionav.util.Observer
import org.slf4j.LoggerFactory

class NavigationService(
    private val positioningService: PositioningService,
    private val routingService: RoutingService
) : Observable<NavigationServiceState> {
    private val logger = LoggerFactory.getLogger(NavigationService::class.java.simpleName)

    private val positionObserver = object : IPositionObserver {
        override fun update(c: IonavLocation?) {
            logger.info("$TAG received position change.")
            recomputeRemainingRoute()
        }
    }

    val initialized: Boolean
        get() = routingService.initialized

    var destination
        get() = state.destination
        set(value) {
            state.destination = value

            if (value != null) {
                positioningService.registerObserver(positionObserver)
            } else {
                positioningService.removeObserver(positionObserver)
            }

            recomputeRemainingRoute()
        }

    var remainingRoute: PathWrapper? = null
        private set


    private fun recomputeRemainingRoute() {
        val lastKnownPosition = positioningService.lastKnownPosition
        val lastKnownPositionCoordinates = lastKnownPosition?.coordinate

        if (lastKnownPosition == null) {
            logger.debug("Last known position is null. Clearing remaining route.")
            remainingRoute = null
            return
        }

        if (destination == null) {
            logger.debug("Destination is null. Clearing remaining route.")
            remainingRoute = null
            return
        }


        state.remainingRoute = routingService.route(lastKnownPositionCoordinates, destination)
        notifyObservers()
    }


    init {
        positioningService.registerObserver(positionObserver)
    }

    companion object {

        const val TAG = "NavigationService"
    }

    private val _observers = mutableListOf<Observer<NavigationServiceState>>()
    override fun registerObserver(observer: Observer<NavigationServiceState>) {
        if (_observers.contains(observer)) return
        _observers.add(observer)
    }

    override fun removeObserver(observer: Observer<NavigationServiceState>) {
        _observers.remove(observer)
    }

    override val state = NavigationServiceState(null,null)


    override fun notifyObservers() {
        _observers.forEach { it.update(state) }
    }

}

