package de.ironjan.arionav_fw.samples.campus

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
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.arionav_fw.samples.campus.viewmodel.NamedPlacesAdapter
import kotlinx.android.synthetic.main.fragment_simple_map_nav.*
import org.slf4j.LoggerFactory

class PlacesFragment : Fragment() {

    private var places: List<String> = emptyList()
    lateinit var dataAdapter: NamedPlacesAdapter

    private val logger = LoggerFactory.getLogger(PlacesFragment::class.java.name)

    private val viewModel: IonavViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_list, container, false)
        val lContext = context ?: return view

        val viewManager = LinearLayoutManager(lContext)
        val onItemClickListener = object : NamedPlacesAdapter.OnItemClickListener {
            override fun onItemClick(placeName: String) {
                logger.info("Clicked on $placeName.")

                when (val destination = viewModel.setDestinationString(placeName)) {
                    null -> Snackbar.make(btnCenterOnUser, "Could not find $placeName.", Snackbar.LENGTH_SHORT).show()
                    else -> {
                        viewModel.setDestinationAndName(placeName, destination)
                        findNavController().navigate(R.id.arEnabledMapViewFragment)
                    }
                }
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

        viewModel.destinations.observe(viewLifecycleOwner, Observer {
            places = it.keys.toList()
            updateAdapter()
        })
    }

    private fun updateAdapter() {
        dataAdapter.replaceData(places)
    }

}