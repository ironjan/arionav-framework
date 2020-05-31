package de.ironjan.arionav_fw.arionav.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.ironjan.arionav_fw.arionav.R
import de.ironjan.arionav_fw.ionav.views.mapview.IonavViewModel
import kotlinx.android.synthetic.main.fragment_ar_view.*

class ArNavFragment : Fragment() {
    private val model: IonavViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_ar_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ar_route_view.observe(model, viewLifecycleOwner)
    }

}
