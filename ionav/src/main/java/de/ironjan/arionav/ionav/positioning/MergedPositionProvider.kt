package de.ironjan.arionav.ionav.positioning

import android.content.Context
import androidx.lifecycle.Lifecycle
import de.ironjan.graphhopper.extensions_core.Coordinate

class MergedPositionProvider(
    context: Context,
    lc: Lifecycle)
    : PositionProviderBaseImplementation(context, lc) {
    private var providers: MutableList<IPositionProvider> = mutableListOf()

    private val observer: IPositionObserverV2 = object : IPositionObserverV2 {
        override fun onPositionChange(c: Coordinate?) {
            notifyObservers()
        }

        override fun onPositionChange(c: Coordinate?, provider: IPositionProvider) {
            notifyObservers()
        }

    }

    override var lastKnownPosition: Coordinate?
        get() = providers.filter { it.lastKnownPosition != null }.last().lastKnownPosition
        set(value) {/* readonly */}

    fun addProvider(provider: IPositionProvider) {
        providers.add(provider)

        provider.registerObserver(observer)
    }

    fun removeProvider(provider: IPositionProvider) {
        providers.remove(provider)

        provider.removeObserver(observer)
    }

    override fun start() {
        providers.map { it.start() }
    }

    override fun stop() {
        providers.map { it.stop() }
    }


}