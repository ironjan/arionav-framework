package de.ironjan.arionav.ionav.positioning

import de.ironjan.graphhopper.extensions_core.Coordinate

interface IPositionProvider {
    val lastKnownPosition : IonavLocation?
    val lastUpdate : Long

    val name: String

    fun registerObserver(observer: IPositionObserver)
    fun removeObserver(observer: IPositionObserver)
    fun notifyObservers()


    fun start()

    fun stop()

}

