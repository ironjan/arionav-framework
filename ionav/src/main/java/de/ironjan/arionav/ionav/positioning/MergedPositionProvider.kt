package de.ironjan.arionav.ionav.positioning

import android.content.Context
import androidx.lifecycle.Lifecycle
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

class MergedPositionProvider(
    context: Context,
    lc: Lifecycle)
    : PositionProviderBaseImplementation(context, lc) {
    override val name: String = MergedPositionProvider::class.java.simpleName

    private val logger = LoggerFactory.getLogger(name)

    private var providers: MutableList<IPositionProvider> = mutableListOf()

    private val observer: IPositionObserverV2 = object : IPositionObserverV2 {
        override fun onPositionChange(c: IonavLocation?) {
            notifyObservers()
        }

        override fun onPositionChange(c: IonavLocation?, provider: IPositionProvider) {
            notifyObservers()
        }
    }

    override var lastKnownPosition: IonavLocation?
        get() = providers.lastOrNull { val newEnough = System.currentTimeMillis() - it.lastUpdate < 30000
            val positionKnown = it.lastKnownPosition != null
            logger.debug("Position from ${it.name} is... known = $positionKnown; newEnough = $newEnough ")
            positionKnown && newEnough
        }?.lastKnownPosition
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