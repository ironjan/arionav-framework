package de.ironjan.arionav_fw.ionav.mapview

import android.graphics.drawable.Drawable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenMapLayer
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.oscim.android.canvas.AndroidGraphics
import org.oscim.core.GeoPoint
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.map.Map
import org.slf4j.LoggerFactory

class DestinationMarkerLayer(private val map: Map, private val markerDrawable: Drawable) : ItemizedLayer<MarkerItem>(map, MarkerSymbol(AndroidGraphics.drawableToBitmap(markerDrawable), 0.5f, 1f)),
    ModelDrivenMapLayer<SimplifiedMapViewState, SimpleMapViewViewModel> {

    private val logger = LoggerFactory.getLogger(DestinationMarkerLayer::class.simpleName)


    override fun observe(viewModel: SimpleMapViewViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.getEndCoordinateLifeData().observe(lifecycleOwner, Observer {
            updateMarkerLayer(it, "destination")

            logger.debug("Updated end coordinate in view to $it.")
        })
    }


    private fun updateMarkerLayer(
        it: Coordinate?,
        title: String = ""
    ) {
        removeAllItems()
        map().updateMap(true)

        if (it == null) return

        val markerItem = MarkerItem(title, "description test", GeoPoint(it.lat, it.lon))
        addItem(markerItem)

        map().updateMap(true)
    }


}