package de.ironjan.arionav_fw.arionav.views

import android.os.Bundle
import android.view.View
import de.ironjan.arionav_fw.ionav.views.NavigationViaInstructionsFragment

open class ArEnabledNavigationViaInstructionsFragment : NavigationViaInstructionsFragment(){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
   bindNavigationModeBottomBarWithAr()
    }
}