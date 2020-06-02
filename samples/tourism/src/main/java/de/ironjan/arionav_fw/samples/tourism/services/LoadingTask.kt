package de.ironjan.arionav_fw.samples.tourism.services

import android.os.AsyncTask
import de.ironjan.arionav_fw.samples.tourism.model.readers.TourismPoiReader
import de.ironjan.graphhopper.extensions_core.Coordinate

class LoadingTask(private val osmFilePath: String, private val callback: (Map<String, Coordinate>) -> Unit) : AsyncTask<String, Void, Map<String, Coordinate>>(){
    override fun doInBackground(vararg params: String): Map<String, Coordinate> {
        return TourismPoiReader().parseOsmFile(osmFilePath)
    }

    override fun onPostExecute(result: Map<String, Coordinate>) = callback(result)
}