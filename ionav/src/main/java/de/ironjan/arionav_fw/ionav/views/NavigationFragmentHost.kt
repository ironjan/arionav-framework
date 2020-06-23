package de.ironjan.arionav_fw.ionav.views

interface NavigationFragmentHost {
    fun goToStartNavigation()
    fun goToMapNavigation()
    fun goToInstrucitons()
    fun goToFeedback()
    fun goToMapView(clearNavigationStack: Boolean)
}