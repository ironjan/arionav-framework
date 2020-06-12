package de.ironjan.arionav_fw.arionav

import de.ironjan.arionav_fw.ionav.views.NavigationFragmentHost

interface ArEnabledNavigationFragmentHost : NavigationFragmentHost {
    fun goToArNav()
}