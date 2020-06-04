package de.ironjan.arionav_fw.arionav.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ViewRenderable
import com.graphhopper.PathWrapper
import com.graphhopper.util.Instruction
import com.graphhopper.util.shapes.GHPoint3D
import de.ironjan.arionav_fw.arionav.R
import de.ironjan.arionav_fw.arionav.arcorelocation.ArionavLocationScene
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenUiComponent
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory
import uk.co.appoly.arcorelocation.LocationMarker
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper

private val PathWrapper?.nextInstruction: Instruction?
    get() = this?.instructions
        ?.take(2)
        ?.last()

private val PathWrapper?.currentInstruction: Instruction?
    get() = this?.instructions
        ?.first()

private val Instruction.nextWaypoint: GHPoint3D
    get() = this.points.take(2).last()

private val Instruction.nextLat: Double
    get() = this.nextWaypoint.lat

private val Instruction.nextLon: Double
    get() = this.nextWaypoint.lon

class ArRouteView : ArSceneView, LifecycleObserver, ModelDrivenUiComponent<IonavViewModel> {

    // region ModelDrivenUiComponent
    private lateinit var viewModel: IonavViewModel
    private lateinit var lifecycleOwner: LifecycleOwner

    private var lastRouteUpdate = 0L

    override fun observe(viewModel: IonavViewModel, lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)

        this.viewModel = viewModel
        this.lifecycleOwner = lifecycleOwner
        locationScene?.observe(viewModel, lifecycleOwner)


        viewModel.route.observe(lifecycleOwner, Observer {
            if (!locationSceneIsSetUp) return@Observer

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastRouteUpdate > FiveSecondsInMillis) {
                updateArRoute(it)
                lastRouteUpdate = currentTime
            }
        })


        viewModel.destination.observe(lifecycleOwner, Observer {
            if (!locationSceneIsSetUp) return@Observer

            updateDestinationMarker(it)
        })
    }

    // endregion

    // region constructors
    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    // endregion


    // region lifecycle handling
    private var installRequested: Boolean = false

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        instructionHelper = InstructionHelper(context ?: return)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        logger.debug("onResume()")
        locationScene?.resume()

        val lActivity = context as? Activity? ?: return
        if (session == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                val session = createArSession(lActivity, installRequested)
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(lActivity)
                    return
                } else {
                    setupSession(session)
                }
            } catch (e: UnavailableException) {
                handleSessionException(lActivity, e)
            }
        }

        try {
            resume()
        } catch (ex: CameraNotAvailableException) {
            showError("Unable to get camera", ex)
            lActivity.finish()
            return
        }

        if (session != null) {
            showLoadingMessage()
        }
        // Add an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        scene?.addOnUpdateListener {
            onArUpdate()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        logger.debug("onPause()")
        locationScene?.pause()
        hideLoadingMessage()
        pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        logger.debug("onDestroy()")
        destroy()
    }
    // endregion

    private val logger = LoggerFactory.getLogger(TAG)

    companion object {
        const val TAG = "ArRouteView"

        private const val maxDistance = 5000

        private const val FiveSecondsInMillis = 5000
    }

    private var locationScene: ArionavLocationScene? = null


    // region ar updates


    private fun onArUpdate() {
        if (locationScene == null) {
            setupLocationScene()
        }

        val frame = arFrame
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

    private var locationSceneIsSetUp: Boolean = false

    private fun setupLocationScene() {
        // If our locationScene object hasn't been setup yet, this is a good time to do it
        // We know that here, the AR components have been initiated.
        val lActivity = context as? Activity ?: return

        locationScene = ArionavLocationScene(lActivity, this)
            .apply {
                if (this@ArRouteView::viewModel.isInitialized
                    && this@ArRouteView::lifecycleOwner.isInitialized
                ) {
                    observe(viewModel, lifecycleOwner)
                }

                locationSceneIsSetUp = true
            }


    }

    // endregion

    //region show error
    private fun showErrorOnRenderableLoadFail(e: Throwable) =
        showError("Unable to load renderables", e)

    private fun showError(text: String, e: Throwable) {
        val lContext = context ?: return
        Toast.makeText(lContext, "$text: ${e.message}", Toast.LENGTH_SHORT).show()
    }
//endregion


//region ARCore-Location DemoUtils

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


    //region loading message
    private var loadingMessageSnackbar: Snackbar? = null

    @SuppressLint("WrongConstant")
    private fun showLoadingMessage() {
        if (loadingMessageSnackbar?.isShownOrQueued == true) {
            return
        }

        val s = "Searching Plane"
        loadingMessageSnackbar =
            Snackbar
                .make(this, s, Snackbar.LENGTH_INDEFINITE)
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

    // region updateLocationScene
    private var currentInstructionMarker: LocationMarker? = null
    private val currentInstructionMarkerSync = Any()

    private lateinit var instructionHelper: InstructionHelper

    private fun updateArRoute(route: PathWrapper?) {
        val nextInstruction = route.nextInstruction ?: return
        val currentInstruction = route.currentInstruction ?: return
        val context = context ?: return

        val lat = nextInstruction.points.first().lat
        val lon = nextInstruction.points.first().lon

        synchronized(currentInstructionMarkerSync) {
            val currentInstructionMarker =
                currentInstructionMarker ?: LocationMarker(lon, lat, Node())
                    .apply {
                        onlyRenderWhenWithin = maxDistance
                        height = 1f

                        ViewRenderable.builder()
                            .setView(context, layoutId)
                            .build()
                            .handle { renderable, throwable ->
                                if (throwable != null) {
                                    showErrorOnRenderableLoadFail(throwable)
                                    return@handle
                                }


                                this.node.renderable = renderable
                                renderable.view.setOnTouchListener { _, _ ->
                                    logger.debug("Touched AR of $currentInstruction ")
                                    true
                                }

                                logger.info("Creating marker for '$currentInstruction' at $lat,$lon.")

                            }
                    }.also {
                        locationScene?.add(it)
                        currentInstructionMarker = it
                    }

            currentInstructionMarker.apply {
                latitude = lat
                longitude = lon

                val viewRenderable = this.node.renderable as? ViewRenderable? ?: return@apply
                updateRenderable(viewRenderable, currentInstruction, nextInstruction)
            }
        }
    }


    private var layoutId = R.layout.view_basic_instruction
    private var updateRenderable: (ViewRenderable, Instruction, Instruction?) -> Unit =
        { renderable: ViewRenderable, currentInstruction: Instruction, nextInstruction: Instruction? ->
            val txtName = renderable.view.findViewById<TextView>(R.id.instructionText)
            val txtDistance = renderable.view.findViewById<TextView>(R.id.instructionDistanceInMeters)
            val instructionImage = renderable.view.findViewById<ImageView>(R.id.instructionImage)

            txtName.text = currentInstruction.name
            txtDistance.text = "%.2fm".format(currentInstruction.distance)


            val sign = if (currentInstruction.points.size > 2) {
                currentInstruction.sign
            } else {
                nextInstruction?.sign ?: InstructionHelper.SIGN_DESTINATION
            }
            instructionImage.setImageDrawable(instructionHelper.getInstructionImageFor(sign))
        }

    fun setInstructionView(layoutId: Int, updateRenderable: (ViewRenderable, Instruction, Instruction?) -> Unit) {
        this.layoutId = layoutId
        this.updateRenderable = updateRenderable
        currentInstructionMarker = null
    }

// endregion

    // region destination marker
    private var destinationMarker: LocationMarker? = null
    private var destinationMarkerSync = Any()
    private var lastDestinationMarkerUpdate = 0


    private fun updateDestinationMarker(coordinate: Coordinate?) {
        coordinate ?: return

        val currentTime = System.currentTimeMillis()
        val delta = currentTime - lastDestinationMarkerUpdate
        if (delta < FiveSecondsInMillis) {
            return
        }

        synchronized(destinationMarkerSync) {

            val destinationMarker =
                this.destinationMarker
                    ?: LocationMarker(coordinate.lat, coordinate.lon, Node())
                        .apply {
                            onlyRenderWhenWithin = 10000

                            ViewRenderable.builder()
                                .setView(context, R.layout.ar_destination_marker)
                                .build()
                                .whenComplete { vr, t ->
                                    if (t != null) {
                                        logger.warn("Could not create destination view renderable: ${t.message}.\n")
                                    }
                                    this.node.renderable = vr
                                }
                        }.also {
                            locationScene?.add(it)
                            destinationMarker = it
                        }

            destinationMarker.apply {
                latitude = coordinate.lat
                longitude = coordinate.lon

                updateDestinationRenderable()

            }
        }
    }

    private fun LocationMarker.updateDestinationRenderable() {
        (node.renderable as? ViewRenderable)?.view?.apply {

            val route = viewModel.route.value?: return@apply

            val textDistance = findViewById<TextView>(R.id.txt_Distance)
            val txtDuration = findViewById<TextView>(R.id.txt_Duration)

            textDistance.text = String.format("%.2fm", route.distance)
            txtDuration.text = InstructionHelper.toReadableTime(route.time)

        }
    }

    // endregion

}
