package de.ironjan.arionav.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav.ionav.GhzExtractor
import de.ironjan.arionav.ionav.special_routing.model.NamedPlace
import de.ironjan.arionav.ionav.special_routing.repository.NamedPlaceRepository
import de.ironjan.arionav.sample.ArionavSampleApplication.Companion.ghzResId
import de.ironjan.arionav.sample.ArionavSampleApplication.Companion.mapName
import de.ironjan.arionav.sample.viewmodel.NamedPlacesAdapter
import org.slf4j.LoggerFactory

class PlacesFragment : Fragment() {
    private var places: List<NamedPlace> = emptyList()
    lateinit var dataAdapter: NamedPlacesAdapter

    val logger = LoggerFactory.getLogger(PlacesFragment::class.java.name)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_wifi_aps, container, false)
        val lContext = context ?: return view

        val viewManager = LinearLayoutManager(lContext)
        val onItemClickListener = object : NamedPlacesAdapter.OnItemClickListener {
            override fun onItemClick(item: NamedPlace) {
                val coordinateStringOf = item.coordinate
                var bundle = bundleOf("selectedPoiCoordinate" to coordinateStringOf)
                logger.info("Clicked on $coordinateStringOf, i.e. $item")
                findNavController().navigate(R.id.action_to_mapFragment, bundle)
            }

        }
        dataAdapter = NamedPlacesAdapter(emptyList(), onItemClickListener)

        view.findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = dataAdapter
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lContext = context ?: return
        Toast.makeText(lContext, "Loading...", Toast.LENGTH_SHORT).show()

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")

        val ghzExtractor = GhzExtractor(lContext.applicationContext, ghzResId, mapName)
        NamedPlaceRepository().getPlaces(ghzExtractor.osmFilePath).observe(lifecycleOwner, Observer {
            places = it.values.toList()
            updateAdapter()
        })
        updateAdapter()
    }

    private fun updateAdapter() {
        dataAdapter.replaceData(places)
    }

}