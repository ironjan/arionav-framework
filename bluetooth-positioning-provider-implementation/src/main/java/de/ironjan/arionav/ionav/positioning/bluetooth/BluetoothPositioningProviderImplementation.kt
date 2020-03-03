package de.ironjan.arionav.ionav.positioning.bluetooth

import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.*
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.positioning.IonavLocation
import de.ironjan.arionav.ionav.positioning.PositionProviderBaseImplementation
import de.ironjan.arionav.ionav.positioning.SignalStrength
import de.ironjan.arionav.ionav.positioning.Trilateraion
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*


/*
FIXME: ensure bt enabled in activity
// Ensures Bluetooth is available on the device and it is enabled. If not,
// displays a dialog requesting user permission to enable Bluetooth.
bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
}

 */
class BluetoothPositioningProviderImplementation(private val context: Context, private val lifecycle: Lifecycle) : PositionProviderBaseImplementation(context, lifecycle) {
    override val name = BLUETOOTH_PROVIDER_NAME


    private val lastScan: MutableLiveData<String> = MutableLiveData("")
    fun getLastScan(): LiveData<String> = lastScan

    private val actualDevices = mutableMapOf<String, SignalStrength>()
    private val actualDevicesLiveData = MutableLiveData(listOf<SignalStrength>())
    fun getDevices(): LiveData<List<SignalStrength>> = actualDevicesLiveData

    private val logger = LoggerFactory.getLogger(BluetoothPositioningProviderImplementation::class.java.simpleName)


    private val tmpIdToCoordinate: Map<String, Coordinate> = mapOf(
        "00:CD:FF:00:37:40" to Coordinate(100.0, 0.0, 0.0), // BR512856
        "00:CD:FF:00:34:D7" to Coordinate(0.0, 100.0, 0.0), // BR513883
        "EC:CD:47:40:AD:DC" to Coordinate(0.0, 0.0, 100.0) // Miband
    )

    private val bluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = ContextCompat.getSystemService(context, BluetoothManager::class.java)
        bluetoothManager?.adapter
    }

    val bcr = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_DISCOVERY_STARTED -> {
                    logger.info("Started BT discovery. Clearing known devices...")
                    updateLastScan()
                }
                ACTION_DISCOVERY_FINISHED -> {
                    logger.info("Finished BT discovery. Scheduling new scan in 15s.")

                    val mainHandler = Handler(Looper.getMainLooper())
                    mainHandler.postDelayed({ triggerScan() }, 15000)

                }
                ACTION_FOUND -> {
                    val device = intent.extras?.get(EXTRA_DEVICE) as BluetoothDevice ?: return
                    val name = intent.extras?.getString(EXTRA_NAME)
                    val rssi = intent.extras?.getShort(EXTRA_RSSI)?.toInt() ?: return

                    val bt = this@BluetoothPositioningProviderImplementation
                    logger.info("Discovered device $device ($name) ${rssi}db with $bt")
                    val address = device.address
                    val coordinate = tmpIdToCoordinate[address]

                    actualDevices[address] = SignalStrength(address, coordinate, rssi)

                    val bestBtDevices = actualDevices.values
                        .sortedBy { -it.rssi }

                    actualDevicesLiveData.value = bestBtDevices

                    updatePositionEstimate()
                }
            }
        }

    }

    override fun start() {
        super.start()

        val intf = IntentFilter().apply {
            addAction(ACTION_DISCOVERY_STARTED)
            addAction(ACTION_DISCOVERY_FINISHED)
            addAction(ACTION_FOUND)
        }

        context.registerReceiver(bcr, intf)
        triggerScan()
    }

    private fun triggerScan() {
        bluetoothAdapter?.startDiscovery()
    }


    private fun updateLastScan() {
        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz)
        val nowAsISO = df.format(Date())
        lastScan.value = nowAsISO
    }

    private fun updatePositionEstimate() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate < minTimeBetweenUpdatesInMillis)
            return

        val bestBtDevices = actualDevices.values

//        val bestDevicesAsString = bestBtDevices.joinToString("; ", prefix = "Best BTs: ") {
//            val device = it.device
//            "${device.address} ${device.name} ${it.rssi} ${calculateSignalLevel(it.rssi)}"
//        }
//        logger.info(bestDevicesAsString)


        val knownCoordinateDevices = bestBtDevices.filter { tmpIdToCoordinate.containsKey(it.deviceId) }
        val signalStrengths = knownCoordinateDevices
            .map {
                val coordinate = tmpIdToCoordinate[it.deviceId] ?: return@map null
                SignalStrength(it.deviceId, coordinate, it.rssi)
            }.filterNotNull()

        val naiveTrilateration = Trilateraion.naiveTrilateration(signalStrengths)
        val newPosition = if (naiveTrilateration == null) null else IonavLocation(name, naiveTrilateration)
        if (differentEnough(lastKnownPosition, newPosition)) {
            lastKnownPosition = newPosition
        }
    }

    private fun differentEnough(lastKnownPosition: IonavLocation?, newPosition: IonavLocation?): Boolean {
        if (lastKnownPosition == null) return true
        if (newPosition == null) return true // we're too far from senders..

        val FiveMetersPrecision = 0.00001

        val latDifferentEnough = lastKnownPosition.latL - newPosition.latL > FiveMetersPrecision
        val lonDifferentEnough = lastKnownPosition.lonL - newPosition.lonL > FiveMetersPrecision
        val horizontalPositionDifferentEnough = latDifferentEnough || lonDifferentEnough

        val lvlDifferent = lastKnownPosition.lvlL != newPosition.lvlL

        return horizontalPositionDifferentEnough || lvlDifferent

    }

    override fun stop() {
        super.stop()
        bluetoothAdapter?.cancelDiscovery()
        context.unregisterReceiver(bcr)
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
        fun calculateSignalLevel(rssi: Int, numLevels: Int = BluetoothPositioningProviderImplementation.numLevels): Int {
            return WifiManager.calculateSignalLevel(rssi, numLevels)
        }

        val BLUETOOTH_PROVIDER_NAME: String = BluetoothPositioningProviderImplementation::class.java.simpleName
    }
}
