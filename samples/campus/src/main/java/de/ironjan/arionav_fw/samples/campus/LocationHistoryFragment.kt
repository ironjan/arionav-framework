package de.ironjan.arionav_fw.samples.campus

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
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.arionav_fw.ionav.views.findViewById

class LocationHistoryFragment: Fragment() {
    protected val viewModel: IonavViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_with_recycler_view, container,  false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Location History"

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