package de.ironjan.arionav.ionav.special_routing.model.readers

import de.ironjan.arionav.ionav.special_routing.model.NamedPlace
import de.ironjan.arionav.ionav.special_routing.model.Poi
import de.ironjan.arionav.ionav.special_routing.model.osm.Node
import de.ironjan.arionav.ionav.special_routing.model.osm.Way
import de.ironjan.graphhopper.extensions_core.Coordinate


class PoiOsmReader : OsmReader<Poi>(
    noWaysFilter,
    poiNodeFilter,
    converter
) {
    companion object {
        internal val noWaysFilter = { _: Way -> false }
        internal val poiNodeFilter =
            { n: Node ->
                val tourismValue = n.tags["tourism"]
                val isTouristicAttraction = "attraction" == tourismValue
                val isTouristicArtwork = "artwork" == tourismValue

                isTouristicAttraction || isTouristicArtwork
            }
        internal val converter =
            { nodes: List<Node>, _: List<Way> ->
                nodes.map {
                    val name = it.tags["name"] ?: "N.N."

                    val lvl = it.tags["level"]?.toDoubleOrNull() ?: 0.0
                    val c = Coordinate(it.lat, it.lon, lvl)
                    Poi(name, c, it.tags)
                }
            }
    }
}