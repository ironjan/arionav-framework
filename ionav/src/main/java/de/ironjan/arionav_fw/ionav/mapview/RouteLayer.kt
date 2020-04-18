package de.ironjan.arionav_fw.ionav.mapview

import org.oscim.backend.canvas.Color
import org.oscim.layers.vector.PathLayer
import org.oscim.layers.vector.geometries.Style
import org.oscim.map.Map


class RouteLayer(map: Map, style: Style): PathLayer(map, style) {
    constructor(map: Map, density: Float, color: Int = Color.GREEN): this(map, defaultStyle(density, color))

    companion object {
        fun defaultStyle(density: Float, color: Int): Style =  Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(color)
            .strokeWidth(4 * density)
            .build()
    }
}