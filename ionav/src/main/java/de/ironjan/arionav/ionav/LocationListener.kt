package de.ironjan.arionav.ionav

import android.content.Context
import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import org.slf4j.LoggerFactory

// Based on https://developer.android.com/topic/libraries/architecture/lifecycle#use-cases
class LocationListener(private val context: Context,
                       private val lifecycle: Lifecycle,
                       private val callback: (Location) -> Unit)
// FIXME implement location updates...
    : LifecycleObserver {
    companion object {
        const val TAG = "LocationListener"
    }
    private val logger = LoggerFactory.getLogger(TAG)

    private var enabled = false

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        logger.debug("start() called.")
        if (enabled) {
            logger.debug("start() called... location tracking is enabled.")
            // connect
        }
    }

    fun enable() {
        logger.debug("enable() called.")
        enabled = true
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            // connect if not connected
            logger.warn("NIY: connect to location provider")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        // disconnect if connected
        logger.warn("NIY: disconnect on stop")
    }


}