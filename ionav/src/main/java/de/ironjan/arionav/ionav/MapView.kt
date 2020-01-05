package de.ironjan.arionav.ionav

import android.content.Context
import android.util.AttributeSet
import org.oscim.android.MapView

class MapView: MapView {
    constructor(context: Context, attrsSet: AttributeSet) : super(context, attrsSet) {}
    constructor(context: Context): super(context, null){}


}
