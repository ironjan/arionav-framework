package de.ironjan.arionav.sample

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.android.synthetic.main.activity_ar_view.*
import org.slf4j.LoggerFactory
import uk.co.appoly.arcorelocation.LocationMarker
import uk.co.appoly.arcorelocation.LocationScene
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import kotlin.math.log

class ArViewActivity : AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {
    private var installRequested: Boolean = false

    private var locationScene: LocationScene? = null
    private var hasFinishedLoading: Boolean = false
    private var poiLayoutRenderable: ViewRenderable? = null
    private var arSceneView: ArSceneView? = null
    private var loadingMessageSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_view)

        arSceneView = findViewById<ArSceneView>(R.id.ar_scene_view)

        loadRenderables()
        addUpdateListenerToSceneView()

        ARLocationPermissionHelper.requestPermission(this)
    }

    // region lifecycle
    override fun onResume() {
        super.onResume()

        locationScene?.resume()

        if (arSceneView?.session == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                val session = createArSession(this, installRequested)
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this)
                    return
                } else {
                    arSceneView?.setupSession(session)
                }
            } catch (e: UnavailableException) {
                handleSessionException(this, e)
            }
        }

        try {
            arSceneView?.resume()
        } catch (ex: CameraNotAvailableException) {
            showError("Unable to get camera", ex)
            finish()
            return
        }

        if (arSceneView?.session != null) {
            showLoadingMessage()
        }
    }

    override fun onPause() {
        super.onPause()
        locationScene?.pause()
        arSceneView?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView?.destroy()
    }
    //endregion


    private fun loadRenderables() {
        // Build a renderable from a 2D View.
        val poiLayout = ViewRenderable.builder()
            .setView(this, R.layout.view_basic_instruction)
            .build()

        CompletableFuture.allOf(poiLayout)
            .handle { _, throwable ->
                // When you build a Renderable, Sceneform loads its resources in the background while
                // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                // before calling get().

                if (throwable != null) {
                    showErrorOnRenderableLoadFail(throwable)
                } else {
                    try {
                        poiLayoutRenderable = poiLayout.get()
                        hasFinishedLoading = true
                    } catch (ex: InterruptedException) {
                        showErrorOnRenderableLoadFail(ex)
                    } catch (ex: ExecutionException) {
                        showErrorOnRenderableLoadFail(ex)
                    } catch (e: Exception) {
                        showErrorOnRenderableLoadFail(e)
                    }
                }
            }
    }

    private fun addUpdateListenerToSceneView() {
        // Add an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView?.apply {
            scene?.addOnUpdateListener {
                onArUpdate()
            }
        }
    }

    private fun ArSceneView.onArUpdate() {
        if (!hasFinishedLoading) {
            return
        }

        if (locationScene == null) {
            setupLocationScene()
        }

        val frame = this.arFrame
        if (frame?.camera?.trackingState != TrackingState.TRACKING) {
            return
        }

        locationScene?.processFrame(frame)

        if (loadingMessageSnackbar != null) {
            for (plane in frame.getUpdatedTrackables(Plane::class.java)) {
                if (plane.trackingState == TrackingState.TRACKING) {
                    hideLoadingMessage()
                }
            }
        }
    }

    private fun setupLocationScene() {
        // If our locationScene object hasn't been setup yet, this is a good time to do it
        // We know that here, the AR components have been initiated.
        locationScene = LocationScene(this, arSceneView)

        // Now lets create our location markers.
        // First, a layout
        addPoi(51.70948, 8.75226, "G74")
        addPoi(51.70986, 8.75436, "G61")
        addPoi(51.70951, 8.75273, "G81")
        addPoi(51.719, 8.7559, "Dom")
        addPoi(51.71536, 8.74495, "Herz-Jesu Kirche")
        addPoi(51.70187, 8.76550, "tp25")
        addPoi(51.70689, 8.77120, "fs")
        addPoi(51.73181, 8.73459, "hni")


//        buildRoute()
    }


    val maxDistance = 5000

    private fun addPoi(lat: Double, lon: Double, poi: String) {
        ViewRenderable.builder()
            .setView(this, R.layout.view_basic_instruction)
            .build()
            .thenAccept { renderable ->
                val txtName = renderable.view.findViewById<TextView>(R.id.instructionText)

                txtName.text = poi

                val base = Node()
                base.renderable = renderable
                renderable.view.setOnTouchListener { _, _ ->
                    Toast.makeText(MainActivity@ this, "$poi touched!", Toast.LENGTH_SHORT).show()
                    true
                }

                val marker = LocationMarker(lon, lat, base)
                marker.apply {
                    setRenderEvent {
                        val eView = renderable.view
                        val txtDistance = eView.findViewById<TextView>(R.id.instructionDistanceInMeters)
                        txtDistance.text = "${it.distance}m"
                        it.scaleModifier = if (it.distance < 100) 1f else 500f / it.distance
                    }
                    onlyRenderWhenWithin = maxDistance
                }

                // Adding the marker
                locationScene?.mLocationMarkers?.add(marker)
            }
    }


    companion object {
        private const val TAG = "MainActivity"
    }

    //region loading message
    private fun showLoadingMessage() {
        if (loadingMessageSnackbar?.isShownOrQueued == true) {
            return
        }

        loadingMessageSnackbar = Snackbar.make(
            MainActivity@ this.findViewById<TextView>(android.R.id.content),
            "Searching Plane",
            Snackbar.LENGTH_INDEFINITE
        )
            .apply {
                view.setBackgroundColor(-0x40cdcdce)
                show()
            }
    }

    private fun hideLoadingMessage() {
        loadingMessageSnackbar?.apply {
            dismiss()
            loadingMessageSnackbar = null
        }

    }
//endregion

    //region show error
    private fun showErrorOnRenderableLoadFail(e: Throwable) =
        showError("Unable to load renderables", e)

    private fun showError(text: String, e: Throwable) =
        Toast.makeText(this, "$text: ${e.message}", Toast.LENGTH_SHORT).show()
//endregion

//region DemoUtils

    private fun handleSessionException(activity: Activity, e: Exception) {
        val msg = when (e) {
            is UnavailableArcoreNotInstalledException -> "Please install ARCore"
            is UnavailableApkTooOldException -> "Please update ARCore"
            is UnavailableSdkTooOldException -> "Please update this app"
            is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
            else -> {
                Log.e(TAG, "Exception: $e")
                "Failed to create AR session"
            }
        }
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Creates an ARCore session. This checks for the CAMERA permission, and if granted, checks the
     * state of the ARCore installation. If there is a problem an exception is thrown. Care must be
     * taken to update the installRequested flag as needed to avoid an infinite checking loop. It
     * should be set to true if null is returned from this method, and called again when the
     * application is resumed.
     *
     * @param activity - the activity currently active.
     * @param installRequested - the indicator for ARCore that when checking the state of ARCore, if
     * an installation was already requested. This is true if this method previously returned
     * null. and the camera permission has been granted.
     */
    @Throws(UnavailableException::class)
    fun createArSession(activity: Activity, installRequested: Boolean): Session? {
        var session: Session? = null
        // if we have the camera permission, create the session
        if (ARLocationPermissionHelper.hasPermission(activity)) {
            when (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> return null
                ArCoreApk.InstallStatus.INSTALLED -> {
                }
            }
            session = Session(activity)
            // IMPORTANT!!!  ArSceneView needs to use the non-blocking update mode.
            val config = Config(session)
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session.configure(config)
        }
        return session
    }
//endregion
}
