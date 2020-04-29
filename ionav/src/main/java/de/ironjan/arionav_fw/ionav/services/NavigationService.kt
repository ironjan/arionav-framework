package de.ironjan.arionav_fw.ionav.services

import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.util.Observable
import de.ironjan.arionav_fw.ionav.util.Observer
import org.slf4j.LoggerFactory

class NavigationService(
    private val positioningService: PositioningService,
    private val routingService: RoutingService
) : Observable<NavigationServiceState>,
    Observer<PositioningServiceState> {
    private val logger = LoggerFactory.getLogger(NavigationService::class.java.simpleName)


    override fun update(t: PositioningServiceState) {
        logger.info("Received positioning service update.")

        recomputeRemainingRoute(t.lastKnownPosition)
    }

    val initialized: Boolean
        get() = routingService.initialized

    var destination
        get() = state.destination
        set(value) {
            state.destination = value

            if (value != null) {
                positioningService.registerObserver(this)
            } else {
                positioningService.removeObserver(this)
            }

            recomputeRemainingRoute(positioningService.lastKnownPosition)
        }

    var remainingRoute: PathWrapper? = null
        private set


    private fun recomputeRemainingRoute(lastKnownPosition: IonavLocation?) {
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
        positioningService.registerObserver(this)
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

    override val state = NavigationServiceState(null, null)


    override fun notifyObservers() {
        _observers.forEach { it.update(state) }
    }

}

