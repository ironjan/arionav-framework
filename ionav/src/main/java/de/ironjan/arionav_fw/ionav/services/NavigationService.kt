package de.ironjan.arionav_fw.ionav.services

import com.graphhopper.PathWrapper
import de.ironjan.arionav_fw.ionav.BuildConfig
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.positioning.IonavLocationDistanceCalculator
import de.ironjan.arionav_fw.ionav.util.Observable
import de.ironjan.arionav_fw.ionav.util.Observer
import org.slf4j.LoggerFactory

class NavigationService(
    private val positioningService: PositioningService,
    private val routingService: RoutingService
) : Observable<NavigationServiceState>,
    Observer<PositioningServiceState> {
    private val logger = LoggerFactory.getLogger(NavigationService::class.java.simpleName)


    private var lastKnownPosition: IonavLocation? = null

    override fun update(state: PositioningServiceState) {
        logger.info("Received positioning service update.")


        if (isDifferentEnough(this.lastKnownPosition, state.lastKnownPosition)) {
            this.lastKnownPosition = state.lastKnownPosition
            recomputeRemainingRoute(lastKnownPosition)
        }
    }

    private fun isDifferentEnough(lastKnownPosition: IonavLocation?, newPosition: IonavLocation?): Boolean {
        if (lastKnownPosition == null) return true
        if (newPosition == null) return false

        val isMoreRecent = newPosition.timestamp - lastKnownPosition.timestamp > 10000
        val isDistantEnough = BuildConfig.DEBUG || IonavLocationDistanceCalculator.distanceBetween(lastKnownPosition, newPosition) > 5

        return isMoreRecent && isDistantEnough
    }

    var destination
        get() = state.destination
        set(value) {
            state = state.copy(destination = value)

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

        val newRemainingRoute = if (lastKnownPosition == null || destination == null) {
            logger.debug("Last known position ($lastKnownPosition) or destination ($destination) is null. Clearing remaining route.")
            null
        } else {
            routingService.route(lastKnownPositionCoordinates!!, destination!!)
        }

        state = state.copy(remainingRoute = newRemainingRoute)
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

    override var state = NavigationServiceState()
        private set(value) {
            field = value
            notifyObservers()
        }


    override fun notifyObservers() {
        _observers.toList().forEach { it.update(state) }
    }

}

