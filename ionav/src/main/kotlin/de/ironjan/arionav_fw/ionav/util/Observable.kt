package de.ironjan.arionav_fw.ionav.util

interface Observable<T> {

    val state: T

    /**
     * Registers a new observer. Will do nothing if the observer is already registered.
     * @param observer the new observer
     */
    fun registerObserver(observer: Observer<T>)


    /**
     * Removes a currently known observer. Will do nothing if the observer is not registered.
     */
    fun removeObserver(observer:  Observer<T>)


    fun notifyObservers()

}