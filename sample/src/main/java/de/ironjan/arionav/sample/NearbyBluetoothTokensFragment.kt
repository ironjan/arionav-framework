package de.ironjan.arionav.sample

import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav.ionav.positioning.IPositionObserver
import de.ironjan.arionav.ionav.positioning.bluetooth.BluetoothProviderImplementation
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException


class NearbyBluetoothTokensFragment : CustomListFragment<ScanResult>(scanResultToString) {
    private lateinit var bluetoothProviderImplementation: BluetoothProviderImplementation
    private val logger = LoggerFactory.getLogger(NearbyBluetoothTokensFragment::class.java.simpleName)
    private val devices = emptyMap<String, String>().toMutableMap()

    private val observer: IPositionObserver = object : IPositionObserver {
        override fun onPositionChange(c: Coordinate?) {
            Toast.makeText(context ?: return, "BT Position: $c", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bluetoothProviderImplementation = BluetoothProviderImplementation(context ?: return, lifecycle)
        bluetoothProviderImplementation.registerObserver(observer)
        bluetoothProviderImplementation.start()

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        bluetoothProviderImplementation.getVisibleBluetoothDevices().observe(lifecycleOwner, Observer {
            dataAdapter.replaceData(it)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothProviderImplementation.removeObserver(observer)
    }

    companion object {
        private val scanResultToString: (ScanResult) -> String = {

            val device = it.device
            val address = device.address
            val rssi = it.rssi

            val strength = BluetoothProviderImplementation.calculateSignalLevel(rssi, BluetoothProviderImplementation.numLevels)
            val s = "$address ${device.name} $rssi , $strength/${BluetoothProviderImplementation.numLevels}"
            s
        }
    }
}