package de.ironjan.arionav_fw.ionav.mapview

import de.ironjan.arionav_fw.ionav.routing.model.indoor_map.IndoorData
import org.oscim.layers.vector.VectorLayer
import org.oscim.map.Map
import org.slf4j.LoggerFactory

/** Inspired by <a href="https://github.com/mapsforge/vtm/blob/master/vtm-jeo/src/org/oscim/layers/OSMIndoorLayer.java">OSMIndoorLayer</a>
 * in mapsforge:vtm-jeo
 */
class IndoorLayer(val map: Map, val indoorData: IndoorData, var selectedLevel: Double) : VectorLayer(map) {

    private val logger = LoggerFactory.getLogger(IndoorLayer::class.simpleName)

}