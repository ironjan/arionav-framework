package de.ironjan.arionav_fw.ionav.mapview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenMapLayer
import org.oscim.map.Map

class UserPositionLayer(private val map: Map)
    : org.oscim.layers.LocationLayer(map, 1f),
    ModelDrivenMapLayer<SimplifiedMapViewState, SimpleMapViewViewModel> {


    override fun observe(viewModel: SimpleMapViewViewModel, lifecycleOwner: LifecycleOwner){
        viewModel.getUserPositionLiveData().observe(lifecycleOwner, Observer {
            if(it == null) {
                map.layers().remove(this)
            }else {
                setPosition(it.lat, it.lon, 1f)

                if(layerNotAdded()){
                    map.layers().add(this)
                }
            }
        })
    }

    private fun layerNotAdded() = !map.layers().contains(this)
}
