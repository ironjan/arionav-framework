package de.ironjan.arionav.ionav.positioning

import de.ironjan.graphhopper.extensions_core.Coordinate

interface IPositionProvider {
    val lastKnownPosition : Coordinate?
    val lastUpdate : Long

    fun registerObserver(observer: IPositionObserver)
    fun removeObserver(observer: IPositionObserver)
    fun notifyObservers()


    fun start()

    fun stop()

}

