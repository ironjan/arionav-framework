package de.ironjan.arionav_fw.samples.tourism

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.arionav.views.ArEnabledMapViewFragment
import de.ironjan.arionav_fw.ionav.views.IonavMapView
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel
import de.ironjan.arionav_fw.samples.tourism.views.PoiLayer
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.slf4j.LoggerFactory

class CustomMapViewFragment : ArEnabledMapViewFragment() {
    override val viewModel by activityViewModels<TourismViewModel>()

    private val logger = LoggerFactory.getLogger(CustomMapViewFragment::class.simpleName)

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

                val name = item.title
                logger.info("long press on $name")

                when(val destination = viewModel.setDestinationString(name)){
                        null -> Snackbar.make(view.findViewById(R.id.btnCenterOnUser), "Could not find $name.", Snackbar.LENGTH_SHORT).show()
                        else -> viewModel.setDestinationAndName(name, destination)
                    }

                val context: Context? = activity ?: return true
                Toast.makeText(context, "$name:\n${item.description}", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onItemSingleTapUp(index: Int, item: MarkerItem?): Boolean {
                if (item == null) return true

                val title = item.title
                logger.info("single tap on $title")

                view.findViewById<EditText>(R.id.edit_destination).setText(title)

                val context: Context? = activity ?: return true
                Toast.makeText(context, "$title:\n${item.description}", Toast.LENGTH_SHORT).show()
                return true
            }

        }
        poiLayer.apply {
            observe(viewModel, viewLifecycleOwner)
            setOnItemGestureListener(listener)
        }
    }
}