package de.ironjan.arionav_fw.ionav.model.readers

import android.os.AsyncTask
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData

class IndoorMapDataLoadingTask(
    private val osmFile: String
    , private val callback: (IndoorData) -> Unit
) : AsyncTask<Void, Void, IndoorData>() {
    override fun doInBackground(vararg params: Void?): IndoorData {
        return IndoorDataReader().parseOsmFile(osmFile)
    }

    override fun onPostExecute(result: IndoorData) = callback(result)

}