package de.ironjan.arionav_fw.samples.tourism

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.ar.sceneform.rendering.ViewRenderable
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.arionav.views.NavigationViaArFragment
import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel
import de.ironjan.arionav_fw.samples.tourism.views.PoiLocationMarker
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread


class TourismNavigationViaArFragment : NavigationViaArFragment() {
    override val viewModel: TourismViewModel by activityViewModels()


    override fun updateInstructionView(
        view: View,
        currentInstruction: Instruction,
        nextInstruction: Instruction
    ) {


        val txtInstruction = view.findViewById<TextView>(de.ironjan.arionav_fw.arionav.R.id.instructionText)
        val instructionText = instructionHelper.getTextFor(nextInstruction)
        txtInstruction.text = instructionText
        txtInstruction.visibility = if (instructionText.isBlank()) View.GONE else View.VISIBLE

        val instructionImage = view.findViewById<ImageView>(de.ironjan.arionav_fw.arionav.R.id.instructionImage)
        instructionImage.setImageDrawable(instructionHelper.getInstructionImageFor(nextInstruction.sign))

        view.findViewById<TextView>(R.id.txtDistance).text = InstructionHelper.toReadableDistance(currentInstruction.distance)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ar_route_view.setInstructionView(R.layout.view_custom_instruction, this::updateInstructionView)
        waitForLocationSceneAndSetup()
    }

    private val logger = LoggerFactory.getLogger("TouriArNav")

    private fun waitForLocationSceneAndSetup() {
        thread(true, name = "wait for location scene") {
            while (ar_route_view.locationScene == null) {
                Thread.sleep(5000)
                logger.info("TouriArNav Waiting for location scene.")
            }

            observeViewModel()
        }
    }

    private fun observeViewModel() {

        Handler(Looper.getMainLooper()).post {
            logger.info("TouriArNav Location scene ready. Binding view model")
            viewModel.destinationNodes.observe(viewLifecycleOwner, Observer { updatePoiArMarkers(it) })
        }
    }

    // region poit markers

    val currentMarkers = mutableListOf<PoiLocationMarker>()

    private fun updatePoiArMarkers(destinationNodes: Map<String, Node>?) {
        destinationNodes ?: return
        val locationScene = ar_route_view.locationScene ?: return

        logger.info("TouriArNav updatePoiArMarkers ready to add")

        val newCurrentMarkers = destinationNodes.values.map { PoiLocationMarker(it) }
        val removeMarkers = currentMarkers.filter { cm -> newCurrentMarkers.none { it.id == cm.id } }
        removeMarkers.forEach { locationScene.remove(it) }

        val addMarkers = newCurrentMarkers.filter { nm -> currentMarkers.none { it.id == nm.id } }
        addMarkers.forEach { createAndAddMarkerToLocationScene(it) }

    }

    private fun createAndAddMarkerToLocationScene(poiLocationMarker: PoiLocationMarker) {
        logger.info("TouriArNav createAndAddMarkerToLocationScene($poiLocationMarker)")

        val context = context ?: return
        logger.info("TouriArNav createAndAddMarkerToLocationScene($poiLocationMarker)... continues")

        poiLocationMarker.apply {
            onlyRenderWhenWithin = 100
            height = 1f

            ViewRenderable.builder()
                .setView(context, R.layout.view_ar_poi)
                .build()
                .handle { viewRenderable, throwable ->
                    if (throwable != null) return@handle

                    val view = viewRenderable.view
                    view.findViewById<TextView>(R.id.txtName).text = poiLocationMarker.osmNode.name
                    logger.info("TouriArNav built view: $poiLocationMarker")
                    poiLocationMarker.node.renderable = viewRenderable

                    view.setOnClickListener { changeDestination(poiLocationMarker) }
                }
            if(BuildConfig.FLAVOR == "withPoiInAr") {
                ar_route_view.locationScene?.add(poiLocationMarker)
                logger.info("TouriArNav Added to location scene: $poiLocationMarker")
            }
        }
    }

    private fun changeDestination(poiLocationMarker: PoiLocationMarker) {
        val node = poiLocationMarker.osmNode

        val coordinate = Coordinate(node.lat, node.lon, 0.0)
        val nonNullName = node.name ?: coordinate.asString()

        viewModel.setDestinationAndName(nonNullName, coordinate)
        logger.info("TouriArNav changed destination to $nonNullName")
    }

    // endregion
}