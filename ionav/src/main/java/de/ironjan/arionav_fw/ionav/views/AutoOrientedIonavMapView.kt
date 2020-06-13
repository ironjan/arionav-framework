package de.ironjan.arionav_fw.ionav.views

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.model.Bearing
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import org.slf4j.LoggerFactory

open class AutoOrientedIonavMapView: IonavMapView {

    // region constructors
    constructor(context: Context, attrsSet: AttributeSet?) : super(context, attrsSet)
    constructor(context: Context) : super(context, null)
    // endregion


    override fun initialize(viewModel: IonavViewModel, longPressCallback: LongPressCallback) {
        super.initialize(viewModel, longPressCallback)

        observeForAutoOrientation()
    }

    private val logger = LoggerFactory.getLogger(AutoOrientedIonavMapView::class.simpleName)

    private fun observeForAutoOrientation() {
        viewModel.route.observe(lifecycleOwner,
        Observer {
            if(it==null) return@Observer

            val A = viewModel.userLocation.value?.coordinate ?: return@Observer
            val B = it.instructions.take(2).last().points.first()


            val mapPosition = map().mapPosition.apply {
                setPosition(B.lat, B.lon)
                setTilt(60f)
                setZoomLevel(18)
                val compute = -Bearing.compute(A.lat, A.lon, B.lat, B.lon)
                logger.info("Bearing: $bearing - new bearing: $compute")

                bearing = compute
            }
            map().mapPosition = mapPosition

            val new_bearing = map().mapPosition.bearing
            logger.info("After setting... $new_bearing")
        })
    }

}