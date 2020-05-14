package de.ironjan.arionav_fw.arionav.arcorelocation

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.Frame
import com.google.ar.sceneform.ArSceneView
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenUiComponent
import de.ironjan.arionav_fw.ionav.views.mapview.IonavViewModel
import uk.co.appoly.arcorelocation.LocationMarker
import uk.co.appoly.arcorelocation.LocationScene

class ArionavLocationScene(context: Activity?, mArSceneView: ArSceneView?) :
    LocationScene(context, mArSceneView),
    ModelDrivenUiComponent<IonavViewModel> {


    private val positioningServiceDeviceLocation = ArionavDeviceLocation(context, this)

    init {
        deviceLocation = positioningServiceDeviceLocation
    }

    override fun observe(viewModel: IonavViewModel, lifecycleOwner: LifecycleOwner) {
        positioningServiceDeviceLocation.observe(viewModel, lifecycleOwner)
    }

    override fun clearMarkers() {
        super.clearMarkers()

//        val scene = mArSceneView.scene
//        val children = scene.children
//        children.forEach {
//            when(it) {
//                is LocationNode -> it.parent?.removeChild(it)
//            }
//        }
    }

    override fun processFrame(frame: Frame?) {
//        clearMarkers()
//        refreshAnchors()
        super.processFrame(frame)
    }
    fun add(marker: LocationMarker) {
        mLocationMarkers.add(marker)
    }
}