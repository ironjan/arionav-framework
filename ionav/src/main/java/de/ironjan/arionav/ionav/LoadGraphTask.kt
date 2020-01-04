package de.ironjan.arionav.ionav

import android.os.AsyncTask
import com.graphhopper.GraphHopper

class LoadGraphTask(private val mapsFolder: String, private val callback: Callback) : AsyncTask<Void, Void, GraphHopper?>() {
    private var hopper: GraphHopper?  = null
    private var exception: Exception? = null

    override fun doInBackground(vararg p0: Void?): GraphHopper? {
        try {
            val tmpHopp = GraphHopper().forMobile()
            tmpHopp.setElevation(true)
            tmpHopp.load(mapsFolder)

            hopper = tmpHopp
            return tmpHopp
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
