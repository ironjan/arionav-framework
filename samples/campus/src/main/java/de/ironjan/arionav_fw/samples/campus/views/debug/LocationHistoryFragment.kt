package de.ironjan.arionav_fw.samples.campus.views.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.views.findViewById
import de.ironjan.arionav_fw.samples.campus.adapters.LocationHistoryAdapter
import de.ironjan.arionav_fw.samples.campus.viewmodel.DebugFragmentsViewModel

class LocationHistoryFragment: Fragment() {
    private val viewModel: DebugFragmentsViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_with_recycler_view, container,  false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Location History"

        // FIXME use safe method
        viewModel.initialize((activity?.applicationContext as? IonavContainerHolder)?.ionavContainer!!)

        val context = context ?: return

        val lifecycleOwner = viewLifecycleOwner

        val locationHistoryAdapter = LocationHistoryAdapter(lifecycleOwner, viewModel)
        findViewById<RecyclerView>(R.id.recycler_view)!!.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = locationHistoryAdapter
        }
    }
}