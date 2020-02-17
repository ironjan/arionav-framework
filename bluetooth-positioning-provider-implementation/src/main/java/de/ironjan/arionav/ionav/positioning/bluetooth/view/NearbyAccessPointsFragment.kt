package de.ironjan.arionav.ionav.positioning.bluetooth.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import de.ironjan.arionav.bluetooth_positioning_provider_implementation.R
import de.ironjan.arionav.ionav.positioning.bluetooth.view_model.MyAdapter
import kotlinx.android.synthetic.main.fragment_nearby_wifi_aps.*

class NearbyAccessPointsFragment : Fragment(R.layout.fragment_nearby_wifi_aps) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lContext = context ?: return
        var viewManager = LinearLayoutManager(lContext)
        val myDataset = arrayOf("a","b", "c")
        val adapter = MyAdapter(myDataset)
        my_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            this.adapter=adapter
        }
    }
}