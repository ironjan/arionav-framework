package de.ironjan.arionav.ionav.positioning.bluetooth

import de.ironjan.arionav.ionav.positioning.IPositionObserver
import de.ironjan.arionav.ionav.positioning.IPositionProvider
import de.ironjan.graphhopper.extensions_core.Coordinate

class BluetoothProviderImplementation : IPositionProvider {
    override val lastKnownPosition: Coordinate?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun registerObserver(observer: IPositionObserver) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeObserver(observer: IPositionObserver) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun notifyObservers() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}