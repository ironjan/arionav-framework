package de.ironjan.arionav_fw.ionav.views.mapview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenMapExtension
import org.oscim.map.Map

class UserPositionLayer(private val map: Map)
    : org.oscim.layers.LocationLayer(map, 1f),
    ModelDrivenMapExtension<IonavViewModel> {


    override fun observe(viewModel: IonavViewModel, lifecycleOwner: LifecycleOwner){
        viewModel.userLocation.observe(lifecycleOwner, Observer {
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
