package de.ironjan.arionav_fw.ionav.model.osm

import de.ironjan.graphhopper.extensions_core.Coordinate

/** Represents a simplified osm way. */
data class Way(val id:Long,
               val nodeRefs: List<Long>,
               val tags: Map<String, String>){
    val name = tags["name"] ?: ""

}