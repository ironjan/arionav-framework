package de.ironjan.arionav_fw.ionav.positioning.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import de.ironjan.arionav_fw.ionav.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.R
import kotlinx.android.synthetic.main.fragment_with_recycler_view.*

class LocationHistoryFragment: Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_with_recycler_view, container,  false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        val positioningService = when(val ionavContainerHolder = activity?.application) {
            is IonavContainerHolder -> ionavContainerHolder.ionavContainer.positioningService
            else -> null
        } ?: return

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")

        val locationHistoryAdapter = LocationHistoryAdapter(lifecycleOwner, positioningService)
        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = locationHistoryAdapter
        }
    }
}