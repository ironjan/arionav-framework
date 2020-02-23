package de.ironjan.arionav.ionav.positioning

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

// Based on https://developer.android.com/topic/libraries/architecture/lifecycle#use-cases
abstract class PositionProviderBaseImplementation(private val context: Context,
                                                  private val lifecycle: Lifecycle)
    : LifecycleObserver, IPositionProvider {

    private var _lastPosition: Coordinate? = null

    override var lastKnownPosition : Coordinate?
      get() = _lastPosition
    protected set(value) { _lastPosition = value }


    companion object {
        const val TAG = "LocationListener"
    }
    private val logger = LoggerFactory.getLogger(TAG)

    private var enabled = false

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    abstract fun start()

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    abstract fun stop()

    internal val observers: MutableList<IPositionObserver> = mutableListOf()


    override fun registerObserver(observer: IPositionObserver) {
        if (observers.contains(observer)) return

        observers.add(observer)
    }

    override fun removeObserver(observer: IPositionObserver) {
        observers.remove(observer)
    }

    override fun notifyObservers() {
        observers.forEach { o ->
            val position = lastKnownPosition ?: return
            o.onPositionChange(position)
        }
    }
}