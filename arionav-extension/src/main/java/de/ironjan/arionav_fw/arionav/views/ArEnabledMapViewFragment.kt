package de.ironjan.arionav_fw.arionav.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.arionav.ArEnabledNavigationHost
import de.ironjan.arionav_fw.arionav.R
import de.ironjan.arionav_fw.ionav.views.MapViewFragment
import kotlinx.android.synthetic.main.ar_enabled_map_view.*

open class   ArEnabledMapViewFragment : MapViewFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.ar_enabled_map_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnArNav.setOnClickListener {
            when (val navHost = activity) {
                is ArEnabledNavigationHost -> navHost.navigateToAr()
                else -> {
                }
            }
        }

        viewModel.instructionText.observe(viewLifecycleOwner, Observer {
//            if(it == null) {
//                instructionText.visibility = View.GONE
//                return@Observer
//            }
//            instructionText.visibility = View.VISIBLE
//            instructionText.text = it
        })

        updateBtnArNavState(false)

        viewModel.route.observe(viewLifecycleOwner, Observer { updateBtnArNavState(it != null && !it.hasErrors())})
    }

    private fun updateBtnArNavState(enabled: Boolean) {
        btnArNav.isEnabled = enabled
        btnArNavWrapper.visibility = boolToVisibility(enabled)
    }

    private fun boolToVisibility(b: Boolean): Int = if(b) View.VISIBLE else View.GONE
}
