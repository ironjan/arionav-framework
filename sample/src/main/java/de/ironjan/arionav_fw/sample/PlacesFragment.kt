package de.ironjan.arionav_fw.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav_fw.ionav.views.mapview.IonavViewModel
import de.ironjan.arionav_fw.sample.viewmodel.NamedPlacesAdapter
import org.slf4j.LoggerFactory

class PlacesFragment : Fragment() {

    private var places: List<String> = emptyList()
    lateinit var dataAdapter: NamedPlacesAdapter

    private val logger = LoggerFactory.getLogger(PlacesFragment::class.java.name)

    private val viewModel: IonavViewModel by activityViewModels()

    private val indoorData by lazy { viewModel.indoorData }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_list, container, false)
        val lContext = context ?: return view

        val viewManager = LinearLayoutManager(lContext)
        val onItemClickListener = object : NamedPlacesAdapter.OnItemClickListener {
            override fun onItemClick(placeName: String) {
                logger.info("Clicked on $placeName.")
                viewModel.setDestinationString(placeName)
                findNavController().navigate(R.id.action_to_map_nav_fragment)
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


        when (val application = activity?.application) {
            is ArionavSampleApplication -> indoorData.observe(viewLifecycleOwner, Observer {
                places = it.names.toList()
                updateAdapter()
            })
        }
        updateAdapter()
    }

    private fun updateAdapter() {
        dataAdapter.replaceData(places)
    }

}