package de.ironjan.arionav.ionav.positioning.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.os.Handler
import androidx.core.content.ContextCompat

/*
FIXME: ensure bt enabled in activity
// Ensures Bluetooth is available on the device and it is enabled. If not,
// displays a dialog requesting user permission to enable Bluetooth.
bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
}

 */
class BluetoothLeSpike(private val context: Context,
                       private val leScanCallback: ScanCallback,
                       private val handler: Handler) {


    private val bluetoothAdapter: BluetoothLeScanner? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = ContextCompat.getSystemService(context, BluetoothManager::class.java)
        bluetoothManager?.adapter?.bluetoothLeScanner
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private var mScanning: Boolean = false

    fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    mScanning = false
                    bluetoothAdapter?.stopScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                bluetoothAdapter?.startScan(leScanCallback)
            }
            else -> {
                mScanning = false
                bluetoothAdapter?.stopScan(leScanCallback)
            }
        }
    }


    companion object{
    const val SCAN_PERIOD: Long = 10000
}
}