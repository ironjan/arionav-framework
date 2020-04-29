package de.ironjan.arionav_fw.ionav.model.readers

import android.os.AsyncTask
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.model.osm.Way
import org.slf4j.LoggerFactory
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class IndoorDataReader : OsmReader() {
    private val logger = LoggerFactory.getLogger(IndoorDataReader::class.simpleName)

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseOsmFile(osmFile: String): IndoorData {

        val start = System.currentTimeMillis()

        val ways = parseWays(osmFile) { w: Way ->
            val hasName = !w.tags["name"].isNullOrBlank()
            val hasLevel = w.tags.containsKey("level")
            val isIndoor = w.tags.containsKey("indoor")
            hasLevel && isIndoor && hasName
        }
        val waysDone = System.currentTimeMillis()
        logger.info("Read ${ways.count()} relevant ways in ${waysDone - start}ms...")

        val relevantNodeRefs = ways.flatMap { it.nodeRefs }.distinct()
        logger.info("The ways contain ${relevantNodeRefs.count()} node references.")

        val nodes = parseNodes(osmFile) { n: Node ->
            val nodeHasLevel = n.tags.containsKey("level")
            val isInWayWithLevel = relevantNodeRefs.contains(n.id)
            val hasName = !n.tags["name"].isNullOrBlank()
            (nodeHasLevel  && hasName) || isInWayWithLevel
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


        return IndoorData(ways, nodes)
    }
}

class IndoorMapDataLoadingTask(
    private val osmFile: String
    , private val callback: (IndoorData) -> Unit
) : AsyncTask<Void, Void, IndoorData>() {
    override fun doInBackground(vararg params: Void?): IndoorData {
        return IndoorDataReader().parseOsmFile(osmFile)
    }

    override fun onPostExecute(result: IndoorData) = callback(result)

    interface OnIndoorMapDataLoaded {
        fun loadCompleted(indoorData: IndoorData)
    }
}