package de.ironjan.arionav_fw.ionav.positioning

/**
 * Shows that a class provides positioning data that can be observed.
 */
interface IPositionObservable {
    /** The last known position */
    val lastKnownPosition: IonavLocation?
    /** Unix timestamp of the last update. */
    val lastUpdate: Long

    /**
     * Registers a new observer. Will do nothing if the observer is already registered.
     * @param observer the new observer
     */
    fun registerObserver(observer: IPositionObserver)

    /**
     * Removes a currently known observer. Will do nothing if the observer is not registered.
     */
    fun removeObserver(observer: IPositionObserver)

    /**
     * Notifies all registered observers about a new change.
     */
    fun notifyObservers()
}