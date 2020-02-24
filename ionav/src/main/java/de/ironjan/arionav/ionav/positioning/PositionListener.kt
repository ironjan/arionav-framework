package de.ironjan.arionav.ionav.positioning

import de.ironjan.graphhopper.extensions_core.Coordinate

/**
 * API is similar to {@see android.location.LocationListener}
 */
interface PositionListener {
    /** Called when the location has changed. */
    fun onPositionChanged(coordinate: Coordinate)

    /** Called when the provider is disabled by the user.
     * TODO decide if this method is useful*/
    fun onProviderDisabled(provider: String)

    /** Called when the provider is enabled by the user.
     * TODO decide if this method is useful*/
    fun onProviderEnabled(provider: String)
}

