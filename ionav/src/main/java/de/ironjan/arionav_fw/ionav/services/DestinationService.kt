package de.ironjan.arionav_fw.ionav.services

import de.ironjan.arionav_fw.ionav.util.Observable
import de.ironjan.arionav_fw.ionav.util.Observer
import de.ironjan.graphhopper.extensions_core.Coordinate

abstract class DestinationService : Observable<DestinationServiceState> {
    /**
     * Tries to convert {@code value} into a {@code Coordinate}.
     *
     * @return the coordinate of the given place or {@code null}
     */
    abstract fun getCoordinate(value: String): Coordinate?


    protected open var destinations: Map<String, Coordinate> = emptyMap()
        protected set(value) {
            field = value
            notifyObservers()
        }

    // region Observable
    private val _observers = mutableListOf<Observer<DestinationServiceState>>()

    override fun registerObserver(observer: Observer<DestinationServiceState>) {
        if (_observers.contains(observer)) return
        _observers.add(observer)
    }

    override fun removeObserver(observer: Observer<DestinationServiceState>) {
        _observers.remove(observer)
    }

    override fun notifyObservers() {
        _observers.forEach { it.update(state) }
    }
    // endregion
}