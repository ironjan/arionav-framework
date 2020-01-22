package de.ironjan.arionav.ionav.positioning

import android.content.Context
import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

// Based on https://developer.android.com/topic/libraries/architecture/lifecycle#use-cases
abstract class PositionListenerBaseImplementation(private val context: Context,
                                         private val lifecycle: Lifecycle,
                                         private val callback: (Coordinate) -> Unit)
    : LifecycleObserver{

    var lastKnownPosition: Coordinate? = null

    companion object {
        const val TAG = "LocationListener"
    }
    private val logger = LoggerFactory.getLogger(TAG)

    private var enabled = false

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    abstract fun start()

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    abstract fun stop()


}