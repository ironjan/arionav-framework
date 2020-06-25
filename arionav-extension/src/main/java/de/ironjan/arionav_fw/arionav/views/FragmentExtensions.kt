package de.ironjan.arionav_fw.arionav.views

import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import de.ironjan.arionav_fw.arionav.ArEnabledNavigationFragmentHost
import de.ironjan.arionav_fw.arionav.R
import de.ironjan.arionav_fw.ionav.views.findViewById

fun Fragment.bindNavigationModeBottomBarWithAr() {
    val navigationFragmentHost = activity as? ArEnabledNavigationFragmentHost
    findViewById<AppCompatButton>(R.id.btnMapNav)?.setOnClickListener { navigationFragmentHost?.goToMapNavigation() }
    findViewById<AppCompatButton>(R.id.btnTextInstructions)?.setOnClickListener { navigationFragmentHost?.goToInstructions() }
    findViewById<AppCompatButton>(R.id.btnArNav)?.setOnClickListener { navigationFragmentHost?.goToArNav() }
}
