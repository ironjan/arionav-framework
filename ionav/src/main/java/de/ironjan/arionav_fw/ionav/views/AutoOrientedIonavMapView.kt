package de.ironjan.arionav_fw.ionav.views

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.model.Bearing
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import org.oscim.core.MapPosition

open class AutoOrientedIonavMapView: IonavMapView {

    // region constructors
    constructor(context: Context, attrsSet: AttributeSet?) : super(context, attrsSet)
    constructor(context: Context) : super(context, null)
    // endregion


    override fun initialize(viewModel: IonavViewModel, longPressCallback: LongPressCallback) {
        super.initialize(viewModel, longPressCallback)

        observeForAutoOrientation()

        userPositionLayer.isEnabled = false
    }

    private fun observeForAutoOrientation() {
        viewModel.route.observe(lifecycleOwner,
        Observer {
            if(it==null) return@Observer

            val firstTwoPoints = it.instructions.take(2)
            val A = viewModel.userLocation.value?.coordinate ?: return@Observer
            val B = firstTwoPoints.last().points.first()


            val mapPosition = MapPosition().apply {
                setPosition(B.lat, B.lon)
//                setTilt(0.75f)
                setZoomLevel(18)
                setBearing(Bearing.compute(A.lat, A.lon, B.lat, B.lon))
            }
            map().mapPosition = mapPosition
        })
    }

}