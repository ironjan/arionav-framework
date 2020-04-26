package de.ironjan.arionav_fw.sample

import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.positioning.wifi.WifiPositionProvider
import de.ironjan.arionav_fw.sample.data.WifiPositioningProviderHardCodedValues
import kotlinx.android.synthetic.main.fragment_custom_list.*


class NearbyAccessPointsFragment : CustomListFragment<ScanResult>({ scanResult ->
    val mac = scanResult.BSSID
    val name = WifiPositioningProviderHardCodedValues.deviceNameMap[mac] ?: scanResult.SSID
    val coord = WifiPositioningProviderHardCodedValues.nameToCoordinatesMap[name]
    "$name $mac ${scanResult.level} - $coord"}
) {

    private lateinit var wifiPositionProvider: WifiPositionProvider


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val positioningService = when(val ionavContainerHolder = activity?.application) {
            is IonavContainerHolder -> ionavContainerHolder.ionavContainer.positioningService
            else -> null
        } ?: return

        wifiPositionProvider = positioningService.getProvider(WifiPositionProvider.WIFI_POSITIONING_PROVIDER) as WifiPositionProvider

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")

        wifiPositionProvider.getVisibleDevices().observe(lifecycleOwner, Observer {
            dataAdapter.replaceData(it)
        })
        additionalInfo.visibility=View.VISIBLE
        wifiPositionProvider.getLastScan().observe(lifecycleOwner, Observer {
            additionalInfo.text = it
        })

    }


}