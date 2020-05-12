package de.ironjan.arionav_fw.ionav.model.readers

import android.os.AsyncTask
import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import com.graphhopper.GraphHopper
import de.ironjan.graphhopper.levelextension.GraphLoader

class LoadGraphTask(private val mapsFolder: String, private val callback: Callback) : AsyncTask<Void, Void, Either<Exception, GraphHopper>>() {

    override fun doInBackground(vararg p0: Void?): Either<Exception, GraphHopper> =
        try {
            Right(GraphLoader.loadExisting(mapsFolder))
        } catch (e: Exception) {
            Left(e)
        }

    override fun onPostExecute(result: Either<Exception, GraphHopper>) {
        super.onPostExecute(result)

        when (result) {
            is Either.Left -> callback.onError(result.a)
            is Either.Right -> callback.onSuccess(result.b)
        }
    }

    interface Callback {
        fun onSuccess(graphHopper: GraphHopper)
        fun onError(exception: java.lang.Exception)
    }
}
