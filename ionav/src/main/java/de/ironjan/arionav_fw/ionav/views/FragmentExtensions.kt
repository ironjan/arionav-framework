package de.ironjan.arionav_fw.ionav.views

import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import de.ironjan.arionav_fw.ionav.R

fun <T: View> Fragment.findViewById(id: Int): T? {
    return this.view?.findViewById<T>(id)
}


fun Fragment.bindNavigationModeBottomBar() {
    val navigationFragmentHost = activity as? NavigationFragmentHost
    findViewById<AppCompatButton>(R.id.btnMapNav)?.setOnClickListener { navigationFragmentHost?.goToMapNavigation() }
    findViewById<AppCompatButton>(R.id.btnTextInstructions)?.setOnClickListener { navigationFragmentHost?.goToInstructions() }
}