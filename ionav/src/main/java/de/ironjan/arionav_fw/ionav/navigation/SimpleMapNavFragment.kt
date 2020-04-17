package de.ironjan.arionav_fw.ionav.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.ironjan.arionav_fw.ionav.R
import kotlinx.android.synthetic.main.fragment_simple_map_nav.*

class SimpleMapNavFragment : Fragment(R.layout.fragment_simple_map_nav) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tmp.setText("Test")
    }


}
