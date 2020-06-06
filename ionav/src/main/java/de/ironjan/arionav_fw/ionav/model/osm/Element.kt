package de.ironjan.arionav_fw.ionav.model.osm

/** Base class for osm elements */
open class Element(val id: Long,
                       val tags: Map<String, String> = emptyMap()) {
  val name = tags["name"]
}