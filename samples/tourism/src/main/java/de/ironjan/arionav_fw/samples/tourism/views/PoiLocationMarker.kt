package de.ironjan.arionav_fw.samples.tourism.views

import com.google.ar.sceneform.Node
import uk.co.appoly.arcorelocation.LocationMarker

class PoiLocationMarker(val osmNode: de.ironjan.arionav_fw.ionav.model.osm.Node) : LocationMarker(osmNode.lon, osmNode.lat, Node()) {
    val id: Long = osmNode.id
    val lat = osmNode.lat
    val lon = osmNode.lon
}