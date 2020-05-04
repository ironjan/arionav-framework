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

    val name = tags["name"] ?: ""

    override val mainCoordinate = Coordinate(lat, lon, lvl)

}