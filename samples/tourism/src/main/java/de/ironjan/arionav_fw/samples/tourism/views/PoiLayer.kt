package de.ironjan.arionav_fw.samples.tourism.views

import android.graphics.drawable.Drawable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenUiComponent
import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel
import org.oscim.android.canvas.AndroidGraphics
import org.oscim.core.GeoPoint
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.slf4j.LoggerFactory


class PoiLayer(map: org.oscim.map.Map, markerDrawable: Drawable) : ItemizedLayer<MarkerItem>(map, MarkerSymbol(AndroidGraphics.drawableToBitmap(markerDrawable), 0.5f, 1f)),
    ModelDrivenUiComponent<TourismViewModel> {
    private lateinit var viewModel: TourismViewModel

    private val logger = LoggerFactory.getLogger(PoiLayer::class.simpleName)

    override fun observe(viewModel: TourismViewModel, lifecycleOwner: LifecycleOwner) {
        this.viewModel = viewModel


        viewModel.destinationNodes.observe(lifecycleOwner, Observer {
            updateMarkers(it)
        })
    }

    private fun updateMarkers(destinations: Map<String, Node>) {
        clearMarkers()

        if (destinations.isNullOrEmpty()) return

        destinations.values.forEach { addItem(createMarkerItem(it)) }

        map().updateMap(true)
    }

    private fun clearMarkers() {
        removeAllItems()
        map().updateMap(true)
    }

    private fun createMarkerItem(it: Node): MarkerItem {
        val description = it.tags.map { t -> "${t.key} = ${t.value}" }.joinToString("\n")
        val markerItem = MarkerItem(it.name, description, GeoPoint(it.lat, it.lon))
        return markerItem
    }
}