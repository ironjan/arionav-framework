package de.ironjan.arionav_fw.arionav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import de.ironjan.arionav_fw.ionav.navigation.SimpleMapViewFragment
import kotlinx.android.synthetic.main.ar_enabled_map_view.*

class ArEnabledMapViewFragment: SimpleMapViewFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.ar_enabled_map_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnArNav.setOnClickListener {
            val activity = activity ?: return@setOnClickListener
            Toast.makeText(activity, "AR", Toast.LENGTH_SHORT).show()
        }
    }
}