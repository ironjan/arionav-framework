package de.ironjan.arionav_fw.sample.model.readers

import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.model.osm.Way
import de.ironjan.arionav_fw.ionav.model.readers.OsmReader
import de.ironjan.arionav_fw.sample.model.touristic.TouristicData
import org.slf4j.LoggerFactory
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

/**
 * An {@see OsmReader} that reads all {@see IndoorData} from an osm-file.
 */
class TouristicDataReader : OsmReader() {
    private val logger = LoggerFactory.getLogger(TouristicDataReader::class.simpleName)

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseOsmFile(osmFile: String): TouristicData {

        val start = System.currentTimeMillis()

        val ways = parseWays(osmFile) { w: Way ->
            val hasName = !w.tags["name"].isNullOrBlank()
            val isTouristic = w.tags.containsKey("tourism")
            isTouristic && hasName
        }
        val waysDone = System.currentTimeMillis()
        logger.info("Read ${ways.count()} relevant ways in ${waysDone - start}ms...")

        val relevantNodeRefs = ways.flatMap { it.nodeRefs }.distinct()
        logger.info("The ways contain ${relevantNodeRefs.count()} node references.")

        val nodes = parseNodes(osmFile) { n: Node ->
            val isTouristic = n.tags.containsKey("toruism")
            val isInWayWithLevel = relevantNodeRefs.contains(n.id)
            val hasName = !n.tags["name"].isNullOrBlank()
            (isTouristic  && hasName) || isInWayWithLevel
        }
        val nodesDone = System.currentTimeMillis()
        logger.info("Read ${nodes.count()} relevant nodes in ${nodesDone - waysDone}ms...")

        val touristicData = convertToIndoorMapData(ways, nodes)


        val convertEnd = System.currentTimeMillis()

        val wayTime = waysDone - start
        val nodeTime = nodesDone - waysDone
        val convertTime = convertEnd - nodesDone

        logger.info("Read ${ways.count()} ways in ${wayTime}ms and ${nodes.count()} nodes in ${nodeTime}ms. Conversion completed after ${convertTime}ms.")

        return touristicData
    }

    private fun convertToIndoorMapData(
        ways: List<Way>,
        nodes: List<Node>
    ): TouristicData {


        return TouristicData(ways, nodes)
    }
}

