package de.ironjan.arionav_fw.ionav.views

import android.view.View
import androidx.fragment.app.Fragment

fun <T: View> Fragment.findViewById(id: Int): T? {
    return this.view?.findViewById<T>(id)
}