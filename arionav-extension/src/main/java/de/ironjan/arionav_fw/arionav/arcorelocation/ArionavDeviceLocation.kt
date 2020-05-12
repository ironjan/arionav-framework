package de.ironjan.arionav_fw.arionav.arcorelocation

import android.content.Context
import android.location.Location
import androidx.lifecycle.LifecycleOwner
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenUiComponent
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.views.mapview.IonavViewModel
import uk.co.appoly.arcorelocation.LocationScene
import uk.co.appoly.arcorelocation.sensor.DeviceLocation

class ArionavDeviceLocation(context: Context?, locationScene: LocationScene?) :
    DeviceLocation(context, locationScene),
    ModelDrivenUiComponent<IonavViewModel> {

    override fun observe(viewModel: IonavViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.userLocation.observe(lifecycleOwner, androidx.lifecycle.Observer {
            onLocationChanged(toLocation(it))
        })
    }


    private fun toLocation(ionavLocation: IonavLocation?): Location? {
        if (ionavLocation == null) return null

        return Location(ionavLocation.provider).apply {
            latitude = ionavLocation.lat
            longitude = ionavLocation.lon
        }
    }

}