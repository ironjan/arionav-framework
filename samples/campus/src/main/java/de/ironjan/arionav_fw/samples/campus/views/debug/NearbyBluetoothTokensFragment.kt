package de.ironjan.arionav_fw.samples.campus.views.debug

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.positioning.SignalStrength
import de.ironjan.arionav_fw.ionav.positioning.bluetooth.BluetoothPositionProvider
import kotlinx.android.synthetic.main.fragment_custom_list.*


class NearbyBluetoothTokensFragment : CustomListFragment<SignalStrength>(signalStrengthToString) {
    private var bluetoothPositionProvider: BluetoothPositionProvider? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Nearby Bluetooth Tokens"

        val positioningService = when(val ionavContainerHolder = activity?.application) {
            is IonavContainerHolder -> ionavContainerHolder.ionavContainer.positioningService
            else -> null
        } ?: return

        val providerImplementation = positioningService.getProvider(BluetoothPositionProvider.BLUETOOTH_PROVIDER_NAME) as BluetoothPositionProvider
        bluetoothPositionProvider = providerImplementation

        providerImplementation.getDevices().observe(viewLifecycleOwner, Observer { replaceData(it) })
        providerImplementation.getLastScan().observe(viewLifecycleOwner, Observer { additionalInfo.text = it })
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