package de.ironjan.arionav.sample

import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav.ionav.positioning.IPositionObserver
import de.ironjan.arionav.ionav.positioning.IonavLocation
import de.ironjan.arionav.ionav.positioning.wifi.WifiPositioningProvider
import de.ironjan.arionav.ionav.positioning.wifi.WifiPositioningProviderHardCodedValues
import de.ironjan.arionav.sample.viewmodel.NearbyAccessPointsViewModel
import kotlinx.android.synthetic.main.fragment_nearby_wifi_aps.*


class NearbyAccessPointsFragment : CustomListFragment<ScanResult>({ scanResult ->
    val mac = scanResult.BSSID
    val name = WifiPositioningProviderHardCodedValues.macsToRooms[mac] ?: scanResult.SSID
    val coord = WifiPositioningProviderHardCodedValues.roomsToCoordinates[name]
    "$name $mac ${scanResult.level} - $coord"}
) {

    private lateinit var wifiPositioningProvider: WifiPositioningProvider
    private val model: NearbyAccessPointsViewModel by activityViewModels()


    private val observer: IPositionObserver = object : IPositionObserver {
        override fun onPositionChange(c: IonavLocation?) {
            Toast.makeText(context ?: return, "BT Position: $c", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wifiPositioningProvider = WifiPositioningProvider(context ?: return, lifecycle)
        wifiPositioningProvider.registerObserver(observer)
        wifiPositioningProvider.start()

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")

        wifiPositioningProvider.getVisibleDevices().observe(lifecycleOwner, Observer {
            dataAdapter.replaceData(it)
        })
        additionalInfo.visibility=View.VISIBLE
        wifiPositioningProvider.getLastScan().observe(lifecycleOwner, Observer {
            additionalInfo.text = it
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        wifiPositioningProvider.stop()
    }

}