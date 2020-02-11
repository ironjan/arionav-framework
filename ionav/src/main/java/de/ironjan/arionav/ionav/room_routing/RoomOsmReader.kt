package de.ironjan.arionav.ionav.room_routing

import android.util.Xml
import de.ironjan.arionav.ionav.room_routing.model.Room
import de.ironjan.arionav.ionav.room_routing.model.osm.Node
import de.ironjan.arionav.ionav.room_routing.model.osm.Way
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.FileInputStream
import java.io.IOException

/**
 * Utility class to read an .osm file and retrieve a list of rooms contained in that file.
 */
class RoomOsmReader {
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseOsmFile(osmFile: String): List<Room> {
        var ways = listOf<Way>()

        val start = System.currentTimeMillis()

        FileInputStream(osmFile).use { fis ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(fis, null)
            parser.nextTag()
            ways = readWays(parser)
            logger.info("Read ${ways.count()} rooms...")
            //val nodes = readRelevantNodes(ways, parser)
        }

        val waysDone = System.currentTimeMillis()

        val ndRefs = ways.flatMap { it.nodeRefs.distinct() }.distinct()

        var nodes = listOf<Node>()
        FileInputStream(osmFile).use { fis ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(fis, null)
            parser.nextTag()


            nodes = readRelevantNodes(ndRefs, parser)
        }

        val nodesDone = System.currentTimeMillis()

        val nodeMap = nodes.map { Pair(it.id, it) }.toMap()

        val rooms = ways.map { w ->
            val name = w.tags["name"]!!

            val doorNodes =
                w.nodeRefs
                    .mapNotNull { nodeMap[it] }
                    .filter {it.tags.containsKey("door") }

            val levelString = w.tags["level"] ?: "0"
            val level = levelString.toDoubleOrNull() ?: 0.0

            val doorCoordinates = doorNodes.map { n ->
                Coordinate(n.lat, n.lon, level)
            }

            val n = nodes.count()
            val lat = nodes.map { it.lat }.sum() / n
            val lon = nodes.map { it.lat }.sum() / n

            val center = Coordinate(lat, lon, 0.0)

            val room = Room(name, center, doorCoordinates)
            logger.debug("Conversion result: $room")
            room
        }
        val convertEnd = System.currentTimeMillis()

        val wayTime = waysDone - start
        val nodeTime = nodesDone - waysDone
        val convertTime = convertEnd - nodesDone

        logger.info("Read ${ways.count()} ways in ${wayTime}ms and ${nodes.count()} nodes in ${nodeTime}ms. Conversion into ${rooms.count()} rooms completed after ${convertTime}ms.")

        return rooms
    }


    private val logger = LoggerFactory.getLogger("RoomRepository")

    private fun readWays(parser: XmlPullParser): List<Way> {
        val ways = mutableListOf<Way>()

        parser.require(XmlPullParser.START_TAG, null, "osm")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "way") {
                val way = readWay(parser)
                if (way != null) {
                    val isRoom =
                        way.tags.containsKey("indoor")
                                && way.tags["indoor"] == "room"
                    val hasName =
                        way.tags.containsKey("name")
                                && way.tags["name"]?.isNotBlank() ?: false

                    if (isRoom && hasName) {
                        logger.debug("Added room $way")
                        ways.add(way)
                    }
                }
            } else {
                skip(parser)
            }
        }
        return ways

    }

    private fun readWay(parser: XmlPullParser): Way? {
        parser.require(XmlPullParser.START_TAG, ns, "way")

        var id: Long? = null
        var nodeRefs = mutableListOf<Long>()
        var tags = mutableMapOf<String, String>()

        id = parser.getAttributeValue(ns, "id").toLongOrNull()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "nd" -> {
                    val nd = readNd(parser)
                    if (nd != null) nodeRefs.add(nd)
                }
                "tag" -> {
                    val tag = readTag(parser)
                    if (tag != null) tags.put(tag.first, tag.second)
                }
                else -> skip(parser)
            }
        }

        return if (id != null) Way(id, nodeRefs, tags) else null
    }

    private fun readNd(parser: XmlPullParser): Long? {
        parser.require(XmlPullParser.START_TAG, ns, "nd")
        val ndRef = parser.getAttributeValue(ns, "ref").toLongOrNull()
        parser.nextTag()
        parser.require(XmlPullParser.END_TAG, ns, "nd")
        return ndRef
    }

    private fun readTag(parser: XmlPullParser): Pair<String, String>? {
        parser.require(XmlPullParser.START_TAG, ns, "tag")
        val key = parser.getAttributeValue(ns, "k")
        val value = parser.getAttributeValue(ns, "v")
        parser.nextTag()
        parser.require(XmlPullParser.END_TAG, ns, "tag")
        return if (key != null && value != null) Pair(key, value) else null
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    private fun readRelevantNodes(ways: List<Long>, parser: XmlPullParser): List<Node> {
        val nodes = mutableListOf<Node>()

        parser.require(XmlPullParser.START_TAG, null, "osm")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "node") {
                val node = readNode(parser)
                if (node != null) {
                    logger.debug("Added node $node")
                    nodes.add(node)
                }
            } else {
                skip(parser)
            }
        }
        return nodes
    }

    private fun readNode(parser: XmlPullParser): Node? {
        parser.require(XmlPullParser.START_TAG, ns, "node")


        val id = parser.getAttributeValue(ns, "id").toLongOrNull()
        val lat = parser.getAttributeValue(ns, "lat").toDoubleOrNull()
        val lon = parser.getAttributeValue(ns, "lon").toDoubleOrNull()
        val tags = mutableMapOf<String, String>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "tag" -> {
                    val tag = readTag(parser)
                    if (tag != null) tags.put(tag.first, tag.second)
                }
                else -> skip(parser)
            }
        }

        return if (id != null && lat != null && lon != null) Node(id, lat, lon, tags) else null
    }


}
