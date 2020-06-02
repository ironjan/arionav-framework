package de.ironjan.arionav_fw.samples.tourism.services

import android.os.AsyncTask
import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.samples.tourism.model.readers.TourismPoiReader
import de.ironjan.graphhopper.extensions_core.Coordinate

class TourismDataLoadingTask(private val osmFilePath: String, private val callback: (Map<String, Node>) -> Unit) : AsyncTask<String, Void, Map<String, Node>>(){
    override fun doInBackground(vararg params: String): Map<String, Node> {
        return TourismPoiReader().parseOsmFile(osmFilePath)
    }

    override fun onPostExecute(result: Map<String, Node>) = callback(result)
}