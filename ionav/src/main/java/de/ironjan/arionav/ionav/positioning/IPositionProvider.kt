package de.ironjan.arionav.ionav.positioning

import androidx.lifecycle.Observer
import de.ironjan.graphhopper.extensions_core.Coordinate

interface IPositionProvider {
    val lastKnownPosition : Coordinate?

    fun registerObserver(observer: IPositionObserver)
    fun removeObserver(observer: IPositionObserver)
    fun notifyObservers()
}

