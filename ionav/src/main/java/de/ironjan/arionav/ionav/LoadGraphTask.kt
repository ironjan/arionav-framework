package de.ironjan.arionav.ionav

import android.os.AsyncTask
import com.graphhopper.GraphHopper
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.util.EncodingManager
import com.graphhopper.routing.util.FlagEncoder
import de.ironjan.graphhopper.levelextension.GraphLoader
import de.ironjan.graphhopper.levelextension.graph.FootFlagLevelEncoder

class LoadGraphTask(private val mapsFolder: String, private val callback: Callback) : AsyncTask<Void, Void, GraphHopper?>() {
    private var hopper: GraphHopper?  = null
    private var exception: Exception? = null

    override fun doInBackground(vararg p0: Void?): GraphHopper? {
        try {
            val tmpHopper = GraphLoader.loadExisting(mapsFolder)
            hopper = tmpHopper
            return hopper
        }
        catch (e: Exception) {
            exception = e
        }
        return null
    }

    override fun onPostExecute(result: GraphHopper?) {
        super.onPostExecute(result)
        val lException = exception
        if(lException != null) {
            callback.onError(lException)
        } else {
            callback.onSuccess(hopper!!)
        }

    }

    companion object {
        const val TAG = "Foo"
    }

    interface Callback {
        fun onSuccess(graphHopper: GraphHopper)
        fun onError(exception: java.lang.Exception)
    }
}
