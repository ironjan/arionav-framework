package de.ironjan.arionav_fw.arionav

import android.content.Context
import android.location.Location
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.services.PositioningServiceState
import de.ironjan.arionav_fw.ionav.util.Observer
import uk.co.appoly.arcorelocation.LocationScene
import uk.co.appoly.arcorelocation.sensor.DeviceLocation

class PositioningServiceDeviceLocation(context: Context?, locationScene: LocationScene?) : DeviceLocation(context, locationScene),
    Observer<PositioningServiceState> {
    override fun update(t: PositioningServiceState) {
        onLocationChanged(toLocation(t.lastKnownPosition))
    }

    private fun toLocation(ionavLocation: IonavLocation?): Location? {
        if (ionavLocation == null) return null

        return Location(ionavLocation.provider).apply {
            latitude = ionavLocation.lat
            longitude = ionavLocation.lon
        }
    }

}