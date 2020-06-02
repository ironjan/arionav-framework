package de.ironjan.arionav_fw.samples.tourism.model.readers

import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.model.osm.Way
import de.ironjan.arionav_fw.ionav.model.readers.IndoorDataReader
import de.ironjan.arionav_fw.ionav.model.readers.OsmReader
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class TourismPoiReader : OsmReader() {
    private val logger = LoggerFactory.getLogger(IndoorDataReader::class.simpleName)

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseOsmFile(osmFile: String): Map<String, Coordinate> {

        val start = System.currentTimeMillis()


        val nodes = parseNodes(osmFile) { n: Node ->
            val isTouristic = n.tags.containsKey("tourism")
            val hasName = !n.tags["name"].isNullOrBlank()
            (isTouristic && hasName)
        }
        val nodesDone = System.currentTimeMillis()
        logger.info("Read ${nodes.count()} relevant nodes in ${nodesDone - start}ms...")

        val touristicPlaces =
            nodes
                .filterNot { it.tags["name"].isNullOrEmpty() }
                .map { Pair(it.tags["name"]!!, Coordinate(it.lat, it.lon, 0.0)) }
                .toMap()


        val convertEnd = System.currentTimeMillis()

        val nodeTime = nodesDone - start
        val convertTime = convertEnd - nodesDone

        logger.info("Read ${nodes.count()} nodes in ${nodeTime}ms. Conversion completed after ${convertTime}ms.")

        return touristicPlaces
    }

}

