package de.ironjan.arionav_fw.arionav.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ironjan.arionav_fw.arionav.ArEnabledNavigationFragmentHost
import de.ironjan.arionav_fw.arionav.R
import de.ironjan.arionav_fw.ionav.views.StartNavigationFragment
import kotlinx.android.synthetic.main.fragment_ar_enabled_start_navigation_fragment.*

open class ArEnabledStartNavigationFragment: StartNavigationFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_ar_enabled_start_navigation_fragment, container, false)

    override val navigationFragmentHost
            get() = activity as? ArEnabledNavigationFragmentHost

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnArNav.setOnClickListener { navigationFragmentHost?.goToArNav() }
    }
}