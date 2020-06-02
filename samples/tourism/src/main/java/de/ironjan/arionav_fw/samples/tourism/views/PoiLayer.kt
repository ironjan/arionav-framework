package de.ironjan.arionav_fw.samples.tourism.views

import android.graphics.drawable.Drawable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenUiComponent
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.oscim.android.canvas.AndroidGraphics
import org.oscim.core.GeoPoint
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.map.Map
import org.slf4j.LoggerFactory

class PoiLayer(private val map: Map, private val markerDrawable: Drawable) : ItemizedLayer<MarkerItem>(map, MarkerSymbol(AndroidGraphics.drawableToBitmap(markerDrawable), 0.5f, 1f)) ,
ModelDrivenUiComponent<IonavViewModel>{
    private val logger = LoggerFactory.getLogger(PoiLayer::class.simpleName)

    override fun observe(viewModel: IonavViewModel, lifecycleOwner: LifecycleOwner) {

        viewModel.destinations.observe(lifecycleOwner, Observer {
            updateMarkers(it)
        })
    }

    private fun updateMarkers(destinations: kotlin.collections.Map<String, Coordinate>) {
        removeAllItems()
        map().updateMap(true)

        if(destinations.isNullOrEmpty()) return

        destinations
            .values
            .forEach{
            val markerItem = MarkerItem("destination", "", GeoPoint(it.lat, it.lon))
            addItem(markerItem)
        }

        map().updateMap(true)
    }
}