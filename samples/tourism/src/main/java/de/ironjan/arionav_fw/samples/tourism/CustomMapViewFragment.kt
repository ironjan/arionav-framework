package de.ironjan.arionav_fw.samples.tourism

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import de.ironjan.arionav_fw.arionav.views.ArEnabledMapViewFragment
import de.ironjan.arionav_fw.ionav.views.IonavMapView
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel
import de.ironjan.arionav_fw.samples.tourism.views.PoiLayer

class CustomMapViewFragment: ArEnabledMapViewFragment() {
    override val viewModel by activityViewModels<TourismViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapView = view.findViewById<IonavMapView>(R.id.mapView)
        val map = mapView.map()
        val poiLayer = PoiLayer(map, resources.getDrawable(R.drawable.marker_icon_blue))
        map.layers().add(poiLayer)
        mapView.isIndoorEnabled = false

        poiLayer.observe(viewModel, viewLifecycleOwner)
    }
}