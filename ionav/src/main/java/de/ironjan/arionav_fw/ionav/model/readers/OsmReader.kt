package de.ironjan.arionav_fw.ionav.model.readers

import android.util.Xml
import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.model.osm.Way
import org.slf4j.LoggerFactory
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.FileInputStream
import java.io.IOException

/**
 * Generic osm-file reader using {@see XmlPullParser}.
 */
abstract class OsmReader<T> {
    private val ns: String? = null
    private val logger = LoggerFactory.getLogger(OsmReader::class.simpleName)

    abstract fun parseOsmFile(osmFile: String): T

    protected  fun parseNodes(
        osmFile: String,
        nodeFilter: (Node) -> Boolean
    ): List<Node> {
        var nodes = listOf<Node>()
        FileInputStream(osmFile).use { fis ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(fis, null)
            parser.nextTag()


            nodes = readNodes(parser, nodeFilter)
        }
        return nodes
    }

    protected fun parseWays(
        osmFile: String,
        namedRoomFilter: (Way) -> Boolean
    ): List<Way> {
        FileInputStream(osmFile).use { fis ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(fis, null)
            parser.nextTag()
            return readWays(parser, namedRoomFilter)
        }
    }

    private fun readWays(parser: XmlPullParser, wayFilter: (Way) -> Boolean): List<Way> {
        val ways = mutableListOf<Way>()

        parser.require(XmlPullParser.START_TAG, null, "osm")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "way") {
                val way = readWay(parser)
                if (way != null) {
                    if (wayFilter(way)) {
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
        val nodeRefs = mutableListOf<Long>()
        val tags = mutableMapOf<String, String>()

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

    private fun readNodes(parser: XmlPullParser, nodeFilter: (n: Node) -> Boolean): List<Node> {
        val nodes = mutableListOf<Node>()

        parser.require(XmlPullParser.START_TAG, null, "osm")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "node") {
                val node = readNode(parser)
                if (node != null && nodeFilter(node)) {
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