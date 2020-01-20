package de.ironjan.arionav.ionav.positioning.gps

import android.content.Context
import android.location.Location
import android.location.LocationListener
import androidx.lifecycle.Lifecycle
import de.ironjan.arionav.ionav.positioning.PositionListenerBaseImplementation
import de.ironjan.graphhopper.extensions_core.Coordinate

class GpsPositionProvider(private val context: Context,
                          private val lifecycle: Lifecycle,
                          private val callback: (Location) -> Unit)
    : PositionListenerBaseImplementation(context, lifecycle, callback),
      LocationListener {
    override fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enable() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPositionChanged(coordinate: Coordinate) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}