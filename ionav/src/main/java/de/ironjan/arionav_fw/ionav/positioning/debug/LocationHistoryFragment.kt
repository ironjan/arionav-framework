package de.ironjan.arionav_fw.ionav.positioning.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.views.mapview.IonavViewModel
import kotlinx.android.synthetic.main.fragment_with_recycler_view.*

class LocationHistoryFragment: Fragment() {
    protected val viewModel: IonavViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_with_recycler_view, container,  false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")

        val locationHistoryAdapter = LocationHistoryAdapter(lifecycleOwner, viewModel)
        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = locationHistoryAdapter
        }
    }
}