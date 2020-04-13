package de.ironjan.arionav_fw.ionav.routing.model.readers

import android.os.AsyncTask
import de.ironjan.arionav_fw.ionav.routing.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.routing.model.indoor_map.IndoorNode
import de.ironjan.arionav_fw.ionav.routing.model.indoor_map.IndoorWay
import de.ironjan.arionav_fw.ionav.routing.model.osm.Node
import de.ironjan.arionav_fw.ionav.routing.model.osm.Way
import org.slf4j.LoggerFactory
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class IndoorDataReader : OsmReader() {
    private val logger = LoggerFactory.getLogger(IndoorDataReader::class.simpleName)

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseOsmFile(osmFile: String): IndoorData {

        val start = System.currentTimeMillis()

        val ways = parseWays(osmFile) { w: Way ->
            w.tags.containsKey("level") && w.tags.containsKey("indoor")
        }
        val waysDone = System.currentTimeMillis()
        logger.info("Read ${ways.count()} relevant ways in ${waysDone - start}ms...")

        val relevantNodeRefs = ways.flatMap { it.nodeRefs }.distinct()
        logger.info("The ways contain ${relevantNodeRefs.count()} node references.")

        val nodes = parseNodes(osmFile) { n: Node ->
            val nodeHasLevel = n.tags.containsKey("level")
            val isInWayWithLevel = relevantNodeRefs.contains(n.id)
            nodeHasLevel || isInWayWithLevel
        }
        val nodesDone = System.currentTimeMillis()
        logger.info("Read ${nodes.count()} relevant nodes in ${nodesDone - waysDone}ms...")

        val indoorData = convertToIndoorMapData(ways, nodes)


        val convertEnd = System.currentTimeMillis()

        val wayTime = waysDone - start
        val nodeTime = nodesDone - waysDone
        val convertTime = convertEnd - nodesDone

        logger.info("Read ${ways.count()} ways in ${wayTime}ms and ${nodes.count()} nodes in ${nodeTime}ms. Conversion completed after ${convertTime}ms.")

        return indoorData
    }

    private fun convertToIndoorMapData(
        ways: List<Way>,
        nodes: List<Node>
    ): IndoorData {

        val indoorNodes = nodes.map {
            val lvl = it.tags["level"]?.toDoubleOrNull() ?: 0.0
            IndoorNode(it.id, it.lat, it.lon, lvl, it.tags)
        }

        val indoorWaysByLevel = ways.map {
            val lvl = it.tags["level"]?.toDoubleOrNull() ?: 0.0
            val nodeRefs = it.nodeRefs.map { nr -> indoorNodes.find { n -> n.id == nr } }.filterNotNull()
            IndoorWay(it.id, lvl, nodeRefs, it.tags)
        }.groupBy { it.lvl }

        val indoorNodesByLevel = indoorNodes.groupBy { it.lvl }

        val indoorWaysByLevelCount = indoorNodesByLevel.map { Pair(it.key ,it.value.count()) }.sortedBy { it.first }.joinToString( ", ")
        logger.info("Ways per level: $indoorWaysByLevelCount")

        return IndoorData(indoorWaysByLevel, indoorNodesByLevel)
    }
}

class IndoorMapDataLoadingTask(
    private val osmFile: String
    , private val callback: OnIndoorMapDataLoaded
) : AsyncTask<Void, Void, IndoorData>() {
    override fun doInBackground(vararg params: Void?): IndoorData {
        return IndoorDataReader().parseOsmFile(osmFile)
    }

    override fun onPostExecute(result: IndoorData) = callback.loadCompleted(result)

    interface OnIndoorMapDataLoaded {
        fun loadCompleted(indoorData: IndoorData)
    }
}