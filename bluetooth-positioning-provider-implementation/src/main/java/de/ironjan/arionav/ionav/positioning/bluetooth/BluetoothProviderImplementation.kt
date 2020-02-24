package de.ironjan.arionav.ionav.positioning.bluetooth

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.positioning.PositionProviderBaseImplementation
import de.ironjan.arionav.ionav.positioning.SignalStrength
import de.ironjan.arionav.ionav.positioning.Trilateraion
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory


/*
FIXME: ensure bt enabled in activity
// Ensures Bluetooth is available on the device and it is enabled. If not,
// displays a dialog requesting user permission to enable Bluetooth.
bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
}

 */
class BluetoothProviderImplementation(private val context: Context, private val lifecycle: Lifecycle)
    : PositionProviderBaseImplementation(context, lifecycle) {
    override val name: String = BluetoothProviderImplementation::class.java.simpleName

    private val devices: MutableMap<String, ScanResult> = mutableMapOf()
    private val listOfVisibleBtDevices: MutableLiveData<List<ScanResult>> = MutableLiveData(listOf())
    fun getVisibleBluetoothDevices(): LiveData<List<ScanResult>> = listOfVisibleBtDevices

    private val logger = LoggerFactory.getLogger(BluetoothProviderImplementation::class.java.simpleName)

    private lateinit var bluetoothLeSpike: BluetoothLeSpike

    private val tmpIdToCoordinate: Map<String, Coordinate> = mapOf(
        "00:CD:FF:00:37:40 BR512856" to Coordinate(100.0, 0.0, 0.0), // BR512856
        "00:CD:FF:00:34:D7" to Coordinate(0.0, 100.0, 0.0), // BR513883
        "EC:CD:47:40:AD:DC" to Coordinate(0.0, 0.0, 100.0) // Miband
    )


    override fun start() {

        val cb = object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)

                if (result == null) return

                val device = result.device
                val address = device.address
                val rssi = result.rssi

                val strength = calculateSignalLevel(rssi, numLevels)
                val s = "$address ${device.name} $rssi , $strength/$numLevels"

                logger.debug("$s ... onScanResult($callbackType, $result)")

                devices[address] = result
                val bestBtDevices = devices.values
                    .sortedBy { -it.rssi }

                listOfVisibleBtDevices.value = bestBtDevices

                updatePositionEstimate()
            }
        }


        bluetoothLeSpike = BluetoothLeSpike(context, cb)

        bluetoothLeSpike.scanLeDevice(true)

    }

    private fun updatePositionEstimate() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate < minTimeBetweenUpdatesInMillis)
            return

        val bestBtDevices = listOfVisibleBtDevices.value!!

        val bestDevicesAsString = bestBtDevices.joinToString("; ", prefix = "Best BTs: ") {
            val device = it.device
            "${device.address} ${device.name} ${it.rssi} ${calculateSignalLevel(it.rssi)}"
        }
        logger.info(bestDevicesAsString)


        val knownCoordinateDevices = bestBtDevices.filter { tmpIdToCoordinate.containsKey(it.device.address) }
        val signalStrengths = knownCoordinateDevices
            .map { SignalStrength(it.device.address, tmpIdToCoordinate[it.device.address]!!, it.rssi) }

        val naiveTrilateration = naiveTrilateration(signalStrengths)
        if(differentEnough(lastKnownPosition, naiveTrilateration)) {
            lastKnownPosition = naiveTrilateration
        }
    }

    private fun differentEnough(lastKnownPosition: Coordinate?, newPosition: Coordinate): Boolean {
        if(lastKnownPosition == null) return true

        val FiveMetersPrecision = 0.00001

        val latDifferentEnough = lastKnownPosition.lat - newPosition.lat > FiveMetersPrecision
        val lonDifferentEnough  = lastKnownPosition.lon - newPosition.lon > FiveMetersPrecision
        val horizontalPositionDifferentEnough = latDifferentEnough || lonDifferentEnough

        val lvlDifferent = lastKnownPosition.lvl != newPosition.lvl

        return horizontalPositionDifferentEnough || lvlDifferent

    }

    private fun naiveTrilateration(devices: List<SignalStrength>): Coordinate {
        return Trilateraion.naiveTrilateration(devices)
    }

    override fun stop() {
        bluetoothLeSpike.scanLeDevice(false)
    }


    companion object {
        const val numLevels = 10

        const val minTimeBetweenUpdatesInMillis = 1000

        /**
         * Calculates the level of the signal. This should be used any time a signal
         * is being shown.
         * From WifiManager.calculateSignalLevel
         * @param rssi The power of the signal measured in RSSI.
         * @param numLevels The number of levels to consider in the calculated
         * level.
         * @return A level of the signal, given in the range of 0 to numLevels-1
         * (both inclusive).
         */
        fun calculateSignalLevel(rssi: Int, numLevels: Int = BluetoothProviderImplementation.numLevels): Int {
            return WifiManager.calculateSignalLevel(rssi, numLevels)
        }
    }
}
