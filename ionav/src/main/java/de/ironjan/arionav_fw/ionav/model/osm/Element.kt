package de.ironjan.arionav_fw.ionav.model.osm

/** Base class for osm elements */
abstract class Element(val id: Long,
                       val tags: Map<String, String> = emptyMap())