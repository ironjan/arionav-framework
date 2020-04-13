package de.ironjan.arionav_fw.ionav.mapview

import de.ironjan.arionav_fw.ionav.routing.model.indoor_map.IndoorData
import org.oscim.layers.vector.VectorLayer
import org.oscim.layers.vector.geometries.PolygonDrawable
import org.oscim.layers.vector.geometries.RectangleDrawable
import org.oscim.layers.vector.geometries.Style
import org.oscim.map.Map

/**
 * Inspired by <a href="https://github.com/mapsforge/vtm/blob/master/vtm-jeo/src/org/oscim/layers/OSMIndoorLayer.java">OSMIndoorLayer</a>
 * in mapsforge:vtm-jeo
 */
class IndoorLayers(private val map: Map, private val density: Float) {

    var indoorData = IndoorData(emptyMap(), emptyMap())
        set(value) {
            field = value
            val prepareLayers = prepareLayers(value)
            levelsToLayers = prepareLayers
            replaceMapLayers(selectedLevel, selectedLevel)
        }

    private var levelsToLayers: kotlin.collections.Map<Double, VectorLayer> = prepareLayers(indoorData)

    private fun prepareLayers(id: IndoorData): kotlin.collections.Map<Double, VectorLayer> {
        return id.levels.map {
            Pair(it, prepareLayer(id, it))
        }.toMap()
    }

    var selectedLevel: Double = 0.0
        set(value) {
            if (value == field) return

            replaceMapLayers(field, value)

            field = value
        }

    private fun replaceMapLayers(oldLevel: Double, newLevel: Double) {
        val oldLayer = levelsToLayers[oldLevel]
        val newLayer = levelsToLayers[newLevel]

        map.layers().remove(oldLayer)
        map.layers().add(newLayer)
    }

    private fun prepareLayer(id: IndoorData, level: Double): VectorLayer {
        val nodeDrawables = id.getNodes(level).map {
            RectangleDrawable(it.toGeoPoint(), it.toGeoPoint())
        }


        val roomStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(-0x66333333)
            .fillColor(-0x33333333)
            .strokeWidth(1 * density)
            .build()

        val wayDrawables = id
            .getWays(level)
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

        val levelLayer = VectorLayer(map)

        val drawables = nodeDrawables.union(wayDrawables)
        drawables.map { levelLayer.add(it) }

        return levelLayer
    }


}