package de.ironjan.arionav_fw.arionav

import android.app.Activity
import com.google.ar.sceneform.ArSceneView
import uk.co.appoly.arcorelocation.LocationScene

class PositioningServiceLocationScene(context: Activity?, mArSceneView: ArSceneView?) : LocationScene(context, mArSceneView) {
  init {
      deviceLocation = PositioningServiceDeviceLocation(context, this)
  }
}