package de.ironjan.arionav_fw.arionav

import android.content.Context
import android.location.Location
import de.ironjan.arionav_fw.ionav.positioning.IPositionObserver
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import uk.co.appoly.arcorelocation.LocationScene
import uk.co.appoly.arcorelocation.sensor.DeviceLocation

class PositioningServiceDeviceLocation(context: Context?, locationScene: LocationScene?) : DeviceLocation(context, locationScene),
    IPositionObserver {
    override fun update(t: IonavLocation?) {
        onLocationChanged(toLocation(t))
    }

    private fun toLocation(ionavLocation: IonavLocation?): Location? {
        if (ionavLocation == null) return null

        val location = Location(ionavLocation.provider).apply {
            latitude = ionavLocation.lat
            longitude = ionavLocation.lon
        }
        return location
    }

}