package de.ironjan.arionav_fw.samples.campus.views

import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.positioning.wifi.WifiPositionProvider
import de.ironjan.arionav_fw.samples.campus.data.WifiPositioningProviderHardCodedValues
import de.ironjan.arionav_fw.samples.campus.views.CustomListFragment
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

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Nearby WiFi APs"

        val positioningService = when(val ionavContainerHolder = activity?.application) {
            is IonavContainerHolder -> ionavContainerHolder.ionavContainer.positioningService
            else -> null
        } ?: return

        wifiPositionProvider = positioningService.getProvider(WifiPositionProvider.WIFI_POSITIONING_PROVIDER) as WifiPositionProvider

        wifiPositionProvider.getVisibleDevices().observe(viewLifecycleOwner, Observer { replaceData(it)         })
        wifiPositionProvider.getLastScan().observe(viewLifecycleOwner, Observer { additionalInfo.text = it })

    }


}