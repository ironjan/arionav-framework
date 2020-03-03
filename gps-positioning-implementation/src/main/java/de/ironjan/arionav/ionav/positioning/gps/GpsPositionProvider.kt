package de.ironjan.arionav.ionav.positioning.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import de.ironjan.arionav.ionav.positioning.IPositionObserver
import de.ironjan.arionav.ionav.positioning.IonavLocation
import de.ironjan.arionav.ionav.positioning.LevelDependentPositionProviderBaseImplementation
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

/**
 *
 * MissingPermission is suppressed because the permission is verified when initializing the gps provider
 */
@SuppressLint("MissingPermission")
open class GpsPositionProvider(
    private val context: Context,
    private val lifecycle: Lifecycle
) : LevelDependentPositionProviderBaseImplementation(context, lifecycle) {
    override val name: String = GpsPositionProvider::class.java.simpleName

    private val observers: MutableList<IPositionObserver> = mutableListOf()

    private val logger = LoggerFactory.getLogger(TAG)

    init {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("Missing Manifest.permission.ACCESS_FINE_LOCATION")
        }

    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val locationListener: LocationListener = object : LocationListener {
        private var currentBestLocation: Location? = null
        private val listenerTag = "GpsPositionProvider.LocationListener"
        private val listenerLogger = LoggerFactory.getLogger(listenerTag)

        override fun onLocationChanged(location: Location?) {
            listenerLogger.debug("onLocationChanged($location) called")

            location ?: return

            if (isBetterLocation(location, currentBestLocation)) {
                currentBestLocation = location
                // FIXME level...
                val lLastKnownPosition = locationToIonavLocation(location)
                lastKnownPosition = lLastKnownPosition
                listenerLogger.debug("Updated current best location and invoked callback.")
            }
            listenerLogger.debug("onLocationChanged($location) finished.")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            /* This method was deprecated in API level 29. This callback will never be invoked */
        }

        override fun onProviderEnabled(provider: String?) {
            listenerLogger.debug("onProviderEnabled($provider)")
            start()
        }

        override fun onProviderDisabled(provider: String?) {
            listenerLogger.debug("onProviderDisabled($provider)")
            stop()
        }

        private val TWO_MINUTES: Long = 1000 * 60 * 2

        /** Determines whether one Location reading is better than the current Location fix. From https://developer.android.com/guide/topics/location/strategies
         * @param location The new Location that you want to evaluate
         * @param currentBestLocation The current Location fix, to which you want to compare the new one
         */
        fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
            listenerLogger.debug("isBetterLocation($location, $currentBestLocation) called")
            if (currentBestLocation == null) {
                // A new location is always better than no location
                return true
            }

            // Check whether the new location fix is newer or older
            val timeDelta: Long = location.time - currentBestLocation.time
            val isSignificantlyNewer: Boolean = timeDelta > TWO_MINUTES
            val isSignificantlyOlder: Boolean = timeDelta < -TWO_MINUTES

            when {
                // If it's been more than two minutes since the current location, use the new location
                // because the user has likely moved
                isSignificantlyNewer -> return true
                // If the new location is more than two minutes older, it must be worse
                isSignificantlyOlder -> return false
            }

            // Check whether the new location fix is more or less accurate
            val isNewer: Boolean = timeDelta > 0L
            val accuracyDelta: Float = location.accuracy - currentBestLocation.accuracy
            val isLessAccurate: Boolean = accuracyDelta > 0f
            val isMoreAccurate: Boolean = accuracyDelta < 0f
            val isSignificantlyLessAccurate: Boolean = accuracyDelta > 200f

            // Check if the old and new location are from the same provider
            val isFromSameProvider: Boolean = location.provider == currentBestLocation.provider

            // Determine location quality using a combination of timeliness and accuracy
            return when {
                isMoreAccurate -> true
                isNewer && !isLessAccurate -> true
                isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
                else -> false
            }
        }
    }

    private val locationProvider = LocationManager.GPS_PROVIDER

    override fun start() {
        if(enabled) return

        logger.debug("start() called.")
        super.start()
        locationManager.requestLocationUpdates(locationProvider, 0L, 0f, locationListener)
        val lastKnownLocation: Location = locationManager.getLastKnownLocation(locationProvider) ?: return
        lastKnownPosition = locationToIonavLocation(lastKnownLocation)
        logger.debug("start() done.")
    }

    override fun stop() {
        if(!enabled) return

        logger.debug("stop() called.")
        super.stop()
        locationManager.removeUpdates(locationListener)
        logger.debug("stop() done.")
    }

    private fun locationToIonavLocation(location: Location) = IonavLocation(name, Coordinate(location.latitude, location.longitude, currentLevel))

    companion object {
        const val TAG = "GpsPositionProvider"
        val GPS_PROVIDER_NAME = GpsPositionProvider::class.java.simpleName
    }
}