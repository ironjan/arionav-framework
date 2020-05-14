package de.ironjan.arionav_fw.arionav.views

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ViewRenderable
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.arionav.R
import de.ironjan.arionav_fw.arionav.arcorelocation.ArionavLocationScene
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import de.ironjan.arionav_fw.ionav.views.mapview.IonavViewModel
import kotlinx.android.synthetic.main.fragment_ar_view.*
import org.slf4j.LoggerFactory
import uk.co.appoly.arcorelocation.LocationMarker
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper

// TODO move logic into view?
class ArNavFragment : Fragment() {
    private var currentInstructionMarker: LocationMarker? = null
    private var locationSceneIsSetUp: Boolean = false

    private var locationScene: ArionavLocationScene? = null
    private var loadingMessageSnackbar: Snackbar? = null

    private val model: IonavViewModel by activityViewModels()

    private lateinit var instructionHelper: InstructionHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_ar_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ar_scene_view.observe(model, viewLifecycleOwner)

        instructionHelper = InstructionHelper(context ?: return)

        addUpdateListenerToSceneView()

        registerLiveDataObservers(viewLifecycleOwner)
    }

    private var lastUpdate = 0L
    private val FiveSecondsInMillis = 5000

    private fun registerLiveDataObservers(lifecycleOwner: LifecycleOwner) {
        model.route.observe(lifecycleOwner, Observer {
            if (!locationSceneIsSetUp) return@Observer

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdate > FiveSecondsInMillis) {
                updateLocationScene()
                lastUpdate = currentTime
            }
        })
    }


    // region lifecycle
    private var installRequested: Boolean = false

    override fun onResume() {
        super.onResume()

        locationScene?.resume()
        val lActivtiy = context as Activity ?: return

        if (ar_scene_view?.session == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                val session = createArSession(lActivtiy, installRequested)
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(lActivtiy)
                    return
                } else {
                    ar_scene_view?.setupSession(session)
                }
            } catch (e: UnavailableException) {
                handleSessionException(lActivtiy, e)
            }
        }

        try {
            ar_scene_view?.resume()
        } catch (ex: CameraNotAvailableException) {
            showError("Unable to get camera", ex)
            lActivtiy.finish()
            return
        }

        if (ar_scene_view?.session != null) {
            showLoadingMessage()
        }
    }

    override fun onPause() {
        super.onPause()
        locationScene?.pause()
        ar_scene_view?.pause()
        hideLoadingMessage()
    }

    override fun onDestroy() {
        super.onDestroy()
        ar_scene_view?.destroy()
    }
    //endregion


    private fun addUpdateListenerToSceneView() {
        // Add an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        ar_scene_view?.apply {
            scene?.addOnUpdateListener {
                onArUpdate()
            }
        }
    }

    private fun onArUpdate() {
        if (locationScene == null) {
            setupLocationScene()
        }

        val frame = ar_scene_view.arFrame
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


    private val logger = LoggerFactory.getLogger(ArNavFragment::class.java.simpleName)

    private fun setupLocationScene() {
        // If our locationScene object hasn't been setup yet, this is a good time to do it
        // We know that here, the AR components have been initiated.
        val lActivity = context as Activity

        locationScene = ArionavLocationScene(lActivity, ar_scene_view)
            .apply { observe(model, viewLifecycleOwner) }


        locationSceneIsSetUp = true
    }


    private fun updateLocationScene() {
        logger.info("Cleared scene")


        val route = model.route.value

        val instruction = route
            ?.instructions
            ?.take(2)
            ?.last() ?: return


        show(instruction)

    }

    private fun show(instruction: Instruction) {
        val context = context ?: return


        ViewRenderable.builder()
            .setView(context, R.layout.view_basic_instruction)
            .build()
            .handle { renderable, throwable ->
                if (throwable != null) {
                    showErrorOnRenderableLoadFail(throwable)
                    return@handle
                }

                updateRenderable(renderable, instruction)

                val base = Node()
                base.renderable = renderable
                renderable.view.setOnTouchListener { _, _ ->
                    logger.debug("Touched AR of $instruction ")
                    true
                }

                val wp = instruction.points.take(2).last()
                val lat = wp.lat
                val lon = wp.lon

                logger.info("Creating marker for '$instruction' at $lat,$lon.")

                if (currentInstructionMarker == null) {
                    val lm = LocationMarker(lon, lat, base)
                        .apply {
                            setRenderEvent {
                                val eView = renderable.view
                                "${it.distance}m"
                                //                        it.scaleModifier = 0.5f // if (it.distance < 100) 1f else 500f / it.distance
                            }
                            onlyRenderWhenWithin = maxDistance
                            height = 2f
                        }

                    locationScene?.apply {
                        add(lm)
                    }

                    currentInstructionMarker = lm
                }

                currentInstructionMarker?.apply {
                    this.latitude = lat
                    this.longitude = lon

                    val viewRenderable = this.node.renderable as? ViewRenderable? ?: return@apply
                    updateRenderable(viewRenderable, instruction)
                }
            }
    }

    private fun updateRenderable(renderable: ViewRenderable, instruction: Instruction) {
        val txtName = renderable.view.findViewById<TextView>(R.id.instructionText)
        val txtDistance = renderable.view.findViewById<TextView>(R.id.instructionDistanceInMeters)
        val instructionImage = renderable.view.findViewById<ImageView>(R.id.instructionImage)

        txtName.text = instruction.name
        txtDistance.text = "%.2fm".format(instruction.distance)
        instructionImage.setImageDrawable(instructionHelper.getInstructionImageFor(instruction.sign))
    }

    //region loading message
    @SuppressLint("WrongConstant")
    private fun showLoadingMessage() {
        if (loadingMessageSnackbar?.isShownOrQueued == true) {
            return
        }

        loadingMessageSnackbar = Snackbar.make(
            ar_scene_view,
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

    private fun showError(text: String, e: Throwable) {
        val lContext = context ?: return
        Toast.makeText(lContext, "$text: ${e.message}", Toast.LENGTH_SHORT).show()
    }
//endregion

//region DemoUtils

    private fun handleSessionException(activity: Activity, e: Exception) {
        val msg = when (e) {
            is UnavailableArcoreNotInstalledException -> "Please install ARCore"
            is UnavailableApkTooOldException -> "Please update ARCore"
            is UnavailableSdkTooOldException -> "Please update this app"
            is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
            else -> {
                Log.e(ArNavFragment::class.java.simpleName, "Exception: $e")
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
            // IMPORTANT!!!  ar_scene_view needs to use the non-blocking update mode.
            val config = Config(session)
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session.configure(config)
        }
        return session
    }

    //endregion
    companion object {
        private const val maxDistance = 5000
    }
}
