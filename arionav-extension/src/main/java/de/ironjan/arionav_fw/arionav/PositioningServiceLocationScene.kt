package de.ironjan.arionav_fw.arionav

import android.app.Activity
import com.google.ar.sceneform.ArSceneView
import de.ironjan.arionav_fw.ionav.services.PositioningService
import uk.co.appoly.arcorelocation.LocationScene

class PositioningServiceLocationScene(context: Activity?, mArSceneView: ArSceneView?, private val positioningService: PositioningService) : LocationScene(context, mArSceneView) {
    private val positioningServiceDeviceLocation = PositioningServiceDeviceLocation(context, this)

    init {
        deviceLocation = positioningServiceDeviceLocation

        positioningService.registerObserver(positioningServiceDeviceLocation)
    }

    override fun resume() {
        super.resume()
        deviceLocation?.also {
            positioningService.registerObserver(positioningServiceDeviceLocation)
        }
    }

    override fun pause() {
        super.pause()
        deviceLocation?.also {
            positioningService.removeObserver(positioningServiceDeviceLocation)
        }
    }
}