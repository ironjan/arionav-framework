package de.ironjan.arionav.ionav.special_routing.model

import de.ironjan.graphhopper.extensions_core.Coordinate

data class Poi(val name: String,
               val coordinate: Coordinate,
               val tags: Map<String, String>)