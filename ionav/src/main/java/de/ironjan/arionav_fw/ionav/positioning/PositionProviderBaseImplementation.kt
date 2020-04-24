package de.ironjan.arionav_fw.ionav.positioning

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import org.slf4j.LoggerFactory

// Based on https://developer.android.com/topic/libraries/architecture/lifecycle#use-cases
abstract class PositionProviderBaseImplementation(
    private val context: Context,
    private val lifecycle: Lifecycle
) : LifecycleObserver, IPositionProvider {

    private var _lastPosition: IonavLocation? = null
    override var lastKnownPosition: IonavLocation?
        get() = _lastPosition
        protected set(value) {
            _lastPosition = value
            lastUpdate = System.currentTimeMillis()
            notifyObservers()
        }

    private var _lastUpdate = 0L
    override var lastUpdate: Long
        get() = _lastUpdate
        protected set(value) {
            _lastUpdate = value
        }


    companion object {
        const val TAG = "LocationListener"
    }

    private val logger = LoggerFactory.getLogger(TAG)

    override var enabled = false

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun start() {
        enabled = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun stop() {
        enabled = false
    }

    private val observers: MutableList<IPositionObserver> = mutableListOf()


    override fun registerObserver(observer: IPositionObserver) {
        if (observers.contains(observer)) return

        observers.add(observer)
    }

    override fun removeObserver(observer: IPositionObserver) {
        observers.remove(observer)
    }

    override fun notifyObservers() {
        logger.debug("$name notifying observers.")
        observers.forEach { o ->
            val position = lastKnownPosition ?: return
            o.update(position)
        }
    }
}