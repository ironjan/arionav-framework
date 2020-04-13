package de.ironjan.arionav_fw.ionav.mapview

import de.ironjan.arionav_fw.ionav.routing.model.indoor_map.IndoorData
import org.oscim.layers.vector.VectorLayer
import org.oscim.layers.vector.geometries.JtsDrawable
import org.oscim.layers.vector.geometries.PolygonDrawable
import org.oscim.layers.vector.geometries.RectangleDrawable
import org.oscim.layers.vector.geometries.Style
import org.oscim.map.Map
import org.slf4j.LoggerFactory

/** Inspired by <a href="https://github.com/mapsforge/vtm/blob/master/vtm-jeo/src/org/oscim/layers/OSMIndoorLayer.java">OSMIndoorLayer</a>
 * in mapsforge:vtm-jeo
 */
class IndoorLayer(private val map: Map, private val indoorData: IndoorData, private val initialLevel: Double, private val density: Float) : VectorLayer(map) {

    private var mCurrentDrawables: MutableList<JtsDrawable> = mutableListOf()

    private val logger = LoggerFactory.getLogger(IndoorLayer::class.simpleName)

    init {
        updateUi()
    }

    var selectedLevel: Double = initialLevel
        get() = field
        set(value) {
            if(value == field) return

            field = value
            updateUi()
        }

    private fun updateUi() {
        val nodeGeometries = indoorData.getNodes(selectedLevel).map {
            RectangleDrawable(it.toGeoPoint(), it.toGeoPoint())
        }


        val roomStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(-0x66333333)
            .fillColor(-0x33333333)
            .strokeWidth(1 * density)
            .build()
        val otherStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(-0x66ff33ff)
            .fillColor(-0x22ff33ff)
            .strokeWidth(1 * density)
            .build()

       val wayGeometries = indoorData
            .getWays(selectedLevel)
            .filter { it.isRoom }
            .map { iw ->
                try {
                    val map1 = iw.nodeRefs.map { it.toGeoPoint() }
                    val geometry = PolygonDrawable(map1)
                    geometry.style = roomStyle

                    geometry
                } catch (e: Exception) {
                    val y = e
                    null
                }
            }.filterNotNull()

        mCurrentDrawables.map { remove(it) }
        mCurrentDrawables.clear()
        mCurrentDrawables.addAll(wayGeometries)
        mCurrentDrawables.addAll(nodeGeometries)
        mCurrentDrawables.map { add(it) }

    }


}