package de.ironjan.arionav_fw.ionav.model.readers

import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.model.osm.Way
import org.slf4j.LoggerFactory
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

open class OsmConverter<T>(private val wayFilter: (Way)->Boolean,
                           private val nodeFilter: (Node)->Boolean,
                           private val  converter: (nodes: List<Node>, ways: List<Way>)-> List<T>) : OsmReader() {
    private  val logger = LoggerFactory.getLogger(OsmConverter::class.simpleName)

    @Throws(XmlPullParserException::class, IOException::class)
     fun parseOsmFile(osmFile: String): List<T> {

        val start = System.currentTimeMillis()

        val ways = parseWays(osmFile, wayFilter)
        val waysDone = System.currentTimeMillis()
        logger.info("Read ${ways.count()} relevant ways in ${waysDone-start}ms...")


        val nodes = parseNodes(osmFile, nodeFilter)
        val nodesDone = System.currentTimeMillis()
        logger.info("Read ${nodes.count()} relevant nodes in ${nodesDone-waysDone}ms...")


        val converted = converter(nodes, ways)
        val convertEnd = System.currentTimeMillis()

        val wayTime = waysDone - start
        val nodeTime = nodesDone - waysDone
        val convertTime = convertEnd - nodesDone

        logger.info("Read ${ways.count()} ways in ${wayTime}ms and ${nodes.count()} nodes in ${nodeTime}ms. Conversion completed after ${convertTime}ms.")

        return converted
    }


}
