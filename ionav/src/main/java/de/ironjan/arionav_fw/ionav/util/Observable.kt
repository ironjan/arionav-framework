package de.ironjan.arionav_fw.ionav.util

interface Observable<T : Observer<V>, V> {

    /**
     * Registers a new observer. Will do nothing if the observer is already registered.
     * @param observer the new observer
     */
    fun registerObserver(observer: T)


    /**
     * Removes a currently known observer. Will do nothing if the observer is not registered.
     */
    fun removeObserver(observer:  T)


    fun notifyObservers(v: V)

}