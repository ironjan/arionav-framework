package de.ironjan.arionav.sample

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav.ionav.positioning.IPositionObserver
import de.ironjan.arionav.ionav.positioning.IonavLocation
import de.ironjan.arionav.ionav.positioning.PositioningProviderRegistry
import de.ironjan.arionav.ionav.positioning.SignalStrength
import de.ironjan.arionav.ionav.positioning.bluetooth.BluetoothPositioningProviderImplementation
import kotlinx.android.synthetic.main.fragment_custom_list.*


class NearbyBluetoothTokensFragment : CustomListFragment<SignalStrength>(signalStrengthToString) {
    private var bluetoothPositioningProviderImplementation: BluetoothPositioningProviderImplementation? = null

    private val observer: IPositionObserver = object : IPositionObserver {
        override fun onPositionChange(c: IonavLocation?) {
            Toast.makeText(context ?: return, "BT Position: $c", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val providerImplementation = PositioningProviderRegistry.Instance.getProvider(BluetoothPositioningProviderImplementation.BLUETOOTH_PROVIDER_NAME) as BluetoothPositioningProviderImplementation
        bluetoothPositioningProviderImplementation = providerImplementation

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        providerImplementation.getDevices().observe(lifecycleOwner, Observer {
            dataAdapter.replaceData(it)
        })
        additionalInfo.visibility=View.VISIBLE
        providerImplementation.getLastScan().observe(lifecycleOwner, Observer { additionalInfo.text = it })
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothPositioningProviderImplementation?.removeObserver(observer)
    }

    companion object {
        private val signalStrengthToString: (SignalStrength) -> String = {

            val address = it.deviceId
            val rssi = it.rssi

            val strength = BluetoothPositioningProviderImplementation.calculateSignalLevel(rssi, BluetoothPositioningProviderImplementation.numLevels)

            val c = it.coordinate?.asString()

            val s = "$address $rssi , $strength/${BluetoothPositioningProviderImplementation.numLevels}: $c"
            s
        }
    }
}