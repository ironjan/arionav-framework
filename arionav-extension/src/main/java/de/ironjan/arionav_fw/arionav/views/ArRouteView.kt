package de.ironjan.arionav_fw.arionav.views

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

import com.google.ar.sceneform.ArSceneView
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenUiComponent
import de.ironjan.arionav_fw.ionav.views.mapview.IonavViewModel
import org.slf4j.LoggerFactory

class ArRouteView : ArSceneView, LifecycleObserver, ModelDrivenUiComponent<IonavViewModel> {
    override fun observe(viewModel: IonavViewModel, lifecycleOwner: LifecycleOwner) {

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        logger.debug("onResume()")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        logger.debug("onPause()")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        logger.debug("onDestroy()")
    }

    var lifecycle: Lifecycle? = null

    private val logger = LoggerFactory.getLogger(TAG)
    companion object {
        const val TAG = "ArRouteView"
    }
}
