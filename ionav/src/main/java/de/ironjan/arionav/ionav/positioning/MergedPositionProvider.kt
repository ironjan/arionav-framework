package de.ironjan.arionav.ionav.positioning

import android.content.Context
import androidx.lifecycle.Lifecycle
import de.ironjan.graphhopper.extensions_core.Coordinate

abstract class MergedPositionProvider(
    context: Context,
    lc: Lifecycle)
    : PositionProviderBaseImplementation(context, lc) {
    private var providers: MutableList<IPositionProvider> = mutableListOf()

    private val observer: IPositionObserverV2 = object : IPositionObserverV2 {
        override fun onPositionChange(c: Coordinate?) {
            // TODO use only the other method
            updatePosition(c, null)
        }

        override fun onPositionChange(c: Coordinate?, provider: IPositionProvider) {
            updatePosition(c, provider)
        }

    }

    private fun updatePosition(c: Coordinate?, provider: IPositionProvider?) {
        if(c==null) return

        // Just use the most recently added provider
        // TODO , *unless* its update is older than 30s (arbitrarily chosen)
        lastKnownPosition = providers.last().lastKnownPosition
    }

    fun addProvider(provider: IPositionProvider) {
        providers.add(provider)

        provider.registerObserver(observer)
    }

    fun removeProvider(provider: IPositionProvider) {
        providers.remove(provider)

        provider.removeObserver(observer)
    }


}