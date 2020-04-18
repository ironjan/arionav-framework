package de.ironjan.arionav_fw.ionav.navigation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.ionav.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.R
import kotlinx.android.synthetic.main.fragment_simple_map_nav.*

class SimpleMapViewFragment : Fragment(R.layout.fragment_simple_map_nav) {


    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Snackbar.make(mapView, "long", Snackbar.LENGTH_LONG)
            .show()
        Snackbar.make(mapView, "short", Snackbar.LENGTH_SHORT)
            .show()

        Snackbar.make(
            btnCenterOnUser,
            "Searching Pla2ne",
            Snackbar.LENGTH_LONG
        ).show()

        btnCenterOnUser.setOnClickListener {

            Snackbar.make(
                btnCenterOnUser,
                "Searching Pla2ne",
                Snackbar.LENGTH_LONG
            ).show()
        }
        mapView.initialize((activity?.application as IonavContainerHolder).ionavContainer)


    }


}
