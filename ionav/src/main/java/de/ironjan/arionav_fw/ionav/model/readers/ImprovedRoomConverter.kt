package de.ironjan.arionav_fw.ionav.model.readers

import de.ironjan.arionav_fw.ionav.model.NamedPlace
import de.ironjan.arionav_fw.ionav.model.Room
import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.model.osm.Way
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

class ImprovedRoomConverter: OsmConverter<NamedPlace>(
    isNamedRoomFilter,
    allNodeFilter,
    osmToRoomConverter
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ImprovedPoiConverter::class.java.simpleName)
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

        private val osmToRoomConverter: (List<Node>, List<Way>) -> List<Room> =
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

                    val center = Coordinate(lat, lon, level)

                    val room = Room(name, center, w.tags, doorCoordinates)
                    logger.debug("Conversion result: $room")
                    room
                }

                rooms.forEach{
                    logger.warn("ROOM ${it.name}: ${it.coordinate.lat} ${it.coordinate.lon} ${it.coordinate.lvl}")
                }

                rooms
            }
    }
}