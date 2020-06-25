package de.ironjan.arionav_fw.ionav.views

import androidx.fragment.app.Fragment

interface NavigationFragmentHost {
    fun goToStartNavigation()
    fun goToMapNavigation()
    fun goToInstructions()
    fun goToFeedback()
    fun goToMapView(clearNavigationStack: Boolean)
}