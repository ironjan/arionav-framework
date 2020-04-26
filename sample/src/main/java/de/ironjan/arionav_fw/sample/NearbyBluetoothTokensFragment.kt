package de.ironjan.arionav_fw.sample

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.positioning.SignalStrength
import de.ironjan.arionav_fw.ionav.positioning.bluetooth.BluetoothPositionProvider
import kotlinx.android.synthetic.main.fragment_custom_list.*


class NearbyBluetoothTokensFragment : CustomListFragment<SignalStrength>(signalStrengthToString) {
    private var bluetoothPositionProvider: BluetoothPositionProvider? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val positioningService = when(val ionavContainerHolder = activity?.application) {
            is IonavContainerHolder -> ionavContainerHolder.ionavContainer.positioningService
            else -> null
        } ?: return

        val providerImplementation = positioningService.getProvider(BluetoothPositionProvider.BLUETOOTH_PROVIDER_NAME) as BluetoothPositionProvider
        bluetoothPositionProvider = providerImplementation

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        providerImplementation.getDevices().observe(lifecycleOwner, Observer {
            dataAdapter.replaceData(it)
        })
        additionalInfo.visibility=View.VISIBLE
        providerImplementation.getLastScan().observe(lifecycleOwner, Observer { additionalInfo.text = it })
    }


    companion object {
        private val signalStrengthToString: (SignalStrength) -> String = {

            val address = it.deviceId
            val rssi = it.rssi

            val strength = BluetoothPositionProvider.calculateSignalLevel(rssi, BluetoothPositionProvider.numLevels)

            val c = it.coordinate?.asString()

            val s = "$address $rssi , $strength/${BluetoothPositionProvider.numLevels}: $c"
            s
        }
    }
}