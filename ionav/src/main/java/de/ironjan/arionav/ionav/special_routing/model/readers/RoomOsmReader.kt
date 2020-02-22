package de.ironjan.arionav.ionav.special_routing.model.readers

import de.ironjan.arionav.ionav.special_routing.model.NamedPlace
import de.ironjan.arionav.ionav.special_routing.model.Room
import de.ironjan.arionav.ionav.special_routing.model.osm.Node
import de.ironjan.arionav.ionav.special_routing.model.osm.Way
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

/**
 * Utility class to read an .osm file and retrieve a list of rooms contained in that file.
 */
class RoomOsmReader : OsmReader<Room>(
    isNamedRoomFilter,
    allNodeFilter,
    osmToRoomConverter
) {


    companion object {
        internal val allNodeFilter = { n: Node -> true }
        internal val isNamedRoomFilter = { w: Way ->
            val isRoom =
                w.tags.containsKey("indoor")
                        && w.tags["indoor"] == "room"
            val hasName =
                w.tags.containsKey("name")
                        && w.tags["name"]?.isNotBlank() ?: false
            isRoom && hasName
        }
        private val logger = LoggerFactory.getLogger(RoomOsmReader::class.java.simpleName)

        internal val osmToRoomConverter: (List<Node>, List<Way>) -> List<Room> =
            { nodes: List<Node>, ways: List<Way> ->

                val nodeMap = nodes.map { Pair(it.id, it) }.toMap()
                val rooms = ways.map { w ->
                    val name = w.tags["name"]!!

                    val roomWayNodeRefs = w.nodeRefs
                        .mapNotNull { nodeMap[it] }

                    val doorNodes =
                        roomWayNodeRefs
                            .filter { it.tags.containsKey("door") }

                    val levelString = w.tags["level"] ?: "0"
                    val level = levelString.toDoubleOrNull() ?: 0.0

                    val doorCoordinates = doorNodes.map { n ->
                        Coordinate(n.lat, n.lon, level)
                    }

                    val n = roomWayNodeRefs.count()
                    val lat = roomWayNodeRefs.map { it.lat }.sum() / n
                    val lon = roomWayNodeRefs.map { it.lon }.sum() / n

                    val center = Coordinate(lat, lon, 0.0)

                    val room = Room(name, center, w.tags, doorCoordinates)
                    logger.debug("Conversion result: $room")
                    room
                }

                rooms
            }


    }
}
