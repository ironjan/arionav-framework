package de.ironjan.arionav_fw.ionav.navigation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
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

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        mapView.onLifecycleOwnerAttached(lifecycleOwner)
        val ionavContainer = (activity?.application as IonavContainerHolder).ionavContainer
        mapView.initialize(ionavContainer)

        btnCenterOnUser.setOnClickListener {
            val coordinate =
                ionavContainer.positioningService
                    .lastKnownLocation
                    .value
                    ?.coordinate
                    ?: return@setOnClickListener

            mapView.centerOn(coordinate)
        }
    }


}
