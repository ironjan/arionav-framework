package de.ironjan.arionav_fw.ionav.model.indoor_map

import de.ironjan.graphhopper.extensions_core.Coordinate
import org.oscim.core.GeoPoint

/** Represents a one-dimensional indoor node */
class IndoorNode(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val lvl: Double,
    val tags: Map<String, String>
) :IndoorPoi{
    fun toGeoPoint(): GeoPoint = GeoPoint(lat, lon)

    val isDoor: Boolean = tags["indoor"] == "door"

    val name = tags["name"] ?: ""

    override val center = Coordinate(lat, lon, lvl)

    val map = tags
        .map { "${it.key}=${it.value}" }
    private val tagsAsString =
        tags
            .map { "${it.key}=${it.value}" }
            .joinToString(",")

    override fun toString(): String = "IndoorNode($id, $lat, $lon, $lvl, $tagsAsString)"
}