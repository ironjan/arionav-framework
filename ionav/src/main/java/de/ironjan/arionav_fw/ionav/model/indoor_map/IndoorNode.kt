package de.ironjan.arionav_fw.ionav.model.indoor_map

import org.oscim.core.GeoPoint

class IndoorNode(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val lvl: Double,
    val tags: Map<String, String>
) {
    fun toGeoPoint(): GeoPoint = GeoPoint(lat, lon)

    val name = tags["name"] ?: ""
}