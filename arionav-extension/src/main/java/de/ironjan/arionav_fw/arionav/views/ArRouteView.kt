package de.ironjan.arionav_fw.arionav.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.rendering.ViewRenderable
import de.ironjan.arionav_fw.arionav.R
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenUiComponent
import de.ironjan.arionav_fw.ionav.views.mapview.IonavViewModel
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

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



    private var hasFinishedLoading: Boolean = false
    private var poiLayoutRenderable: ViewRenderable? = null
    fun loadRenderables() {
        val lContext = context ?: return

        // Build a renderable from a 2D View.
        val poiLayout = ViewRenderable.builder()
            .setView(lContext, R.layout.view_basic_instruction)
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


    //region show error
    private fun showErrorOnRenderableLoadFail(e: Throwable) =
        showError("Unable to load renderables", e)

    private fun showError(text: String, e: Throwable) {
        val lContext = context ?: return
        Toast.makeText(lContext, "$text: ${e.message}", Toast.LENGTH_SHORT).show()
    }
//endregion

}
