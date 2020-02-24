package de.ironjan.arionav.ionav.routing.model.readers

import dagger.Module
import de.ironjan.arionav.ionav.routing.model.NamedPlace
import de.ironjan.arionav.ionav.routing.model.Poi
import de.ironjan.arionav.ionav.routing.model.osm.Node
import de.ironjan.arionav.ionav.routing.model.osm.Way
import de.ironjan.graphhopper.extensions_core.Coordinate
import javax.inject.Inject

@Module
class ImprovedPoiReader @Inject constructor()  : OsmReader<NamedPlace>(
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