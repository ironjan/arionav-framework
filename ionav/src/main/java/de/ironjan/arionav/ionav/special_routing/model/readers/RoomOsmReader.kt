package de.ironjan.arionav.ionav.special_routing.model.readers

import de.ironjan.arionav.ionav.special_routing.model.Room
import de.ironjan.arionav.ionav.special_routing.model.osm.Node
import de.ironjan.arionav.ionav.special_routing.model.osm.Way
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

/**
 * Utility class to read an .osm file and retrieve a list of rooms contained in that file.
 */
class RoomOsmReader : OsmReader<List<Room>>(
    isNamedRoomFilter,
    allNodeFilter,
    osmToRoomConverter
) {


    companion object {
        private val allNodeFilter = { n: Node -> true }
        private val isNamedRoomFilter = { w: Way ->
            val isRoom =
                w.tags.containsKey("indoor")
                        && w.tags["indoor"] == "room"
            val hasName =
                w.tags.containsKey("name")
                        && w.tags["name"]?.isNotBlank() ?: false
            isRoom && hasName
        }
        private val logger = LoggerFactory.getLogger(RoomOsmReader::class.java.simpleName)

        private val osmToRoomConverter: (List<Node>, List<Way>) -> List<Room> =
            { nodes: List<Node>, ways: List<Way> ->

                val nodeMap = nodes.map { Pair(it.id, it) }.toMap()
                val rooms = ways.map { w ->
                    val name = w.tags["name"]!!

                    val doorNodes =
                        w.nodeRefs
                            .mapNotNull { nodeMap[it] }
                            .filter { it.tags.containsKey("door") }

                    val levelString = w.tags["level"] ?: "0"
                    val level = levelString.toDoubleOrNull() ?: 0.0

                    val doorCoordinates = doorNodes.map { n ->
                        Coordinate(n.lat, n.lon, level)
                    }

                    val n = nodes.count()
                    val lat = nodes.map { it.lat }.sum() / n
                    val lon = nodes.map { it.lat }.sum() / n

                    val center = Coordinate(lat, lon, 0.0)

                    val room = Room(name, center, doorCoordinates, w.tags)
                    logger.debug("Conversion result: $room")
                    room
                }

                rooms
            }


    }
}
