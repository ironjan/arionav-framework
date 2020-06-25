package de.ironjan.arionav_fw.samples.tourism

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import de.ironjan.arionav_fw.ionav.views.IonavMapView
import de.ironjan.arionav_fw.ionav.views.ViewMapFragment
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel
import de.ironjan.arionav_fw.samples.tourism.views.PoiLayer
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.slf4j.LoggerFactory

class TourismViewMapFragment : ViewMapFragment() {
    override val viewModel by activityViewModels<TourismViewModel>()

    private val logger = LoggerFactory.getLogger(TourismViewMapFragment::class.simpleName)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapView = view.findViewById<IonavMapView>(R.id.mapView)
        val map = mapView.map()
        val poiLayer = PoiLayer(map, resources.getDrawable(R.drawable.marker_icon_blue))
        map.layers().add(poiLayer)
        mapView.isIndoorEnabled = false

        val listener = object : ItemizedLayer.OnItemGestureListener<MarkerItem> {
            override fun onItemLongPress(index: Int, item: MarkerItem?): Boolean {
                if (item == null) return true
                setDestinationStringAndStartNavigate(item.title)
                return true
            }

            override fun onItemSingleTapUp(index: Int, item: MarkerItem?): Boolean {
                if (item == null) return true
                setDestinationString(item.title)
                return true
            }

        }
        poiLayer.apply {
            observe(viewModel, viewLifecycleOwner)
            setOnItemGestureListener(listener)
        }
    }
}