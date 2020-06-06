package de.ironjan.arionav_fw.ionav.model.indoor_map

import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.oscim.core.GeoPoint

/** Represents a one-dimensional indoor node */
open class IndoorNode(
    id: Long,
    lat: Double,
    lon: Double,
    val lvl: Double,
    tags: Map<String, String>
) : Node(id, lat,lon,tags), IndoorPoi{
    fun toGeoPoint(): GeoPoint = GeoPoint(lat, lon)

    val isDoor: Boolean = tags["indoor"] == "door"

    override val center = Coordinate(lat, lon, lvl)

    val map = tags
        .map { "${it.key}=${it.value}" }
    private val tagsAsString =
        tags
            .map { "${it.key}=${it.value}" }
            .joinToString(",")

    override fun toString(): String = "IndoorNode($id, $lat, $lon, $lvl, $tagsAsString)"
}