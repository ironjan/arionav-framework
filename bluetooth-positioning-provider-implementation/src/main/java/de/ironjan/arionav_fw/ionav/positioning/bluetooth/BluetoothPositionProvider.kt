package de.ironjan.arionav_fw.ionav.positioning.bluetooth

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
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.positioning.PositionProviderBaseImplementation
import de.ironjan.arionav_fw.ionav.positioning.SignalStrength
import de.ironjan.arionav_fw.ionav.positioning.Trilateraion
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
class BluetoothPositionProvider(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val deviceMap: Map<String, Coordinate>
) : PositionProviderBaseImplementation(context, lifecycle) {
    override val name = BLUETOOTH_PROVIDER_NAME


    private val lastScan: MutableLiveData<String> = MutableLiveData("")
    fun getLastScan(): LiveData<String> = lastScan

    private val actualDevices = mutableMapOf<String, SignalStrength>()
    private val actualDevicesLiveData = MutableLiveData(listOf<SignalStrength>())
    fun getDevices(): LiveData<List<SignalStrength>> = actualDevicesLiveData

    private val logger = LoggerFactory.getLogger(BluetoothPositionProvider::class.java.simpleName)


    private val bluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = ContextCompat.getSystemService(context, BluetoothManager::class.java)
        bluetoothManager?.adapter
    }

    val bcr = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_DISCOVERY_STARTED -> {
                    scanStarted()
                }
                ACTION_DISCOVERY_FINISHED -> {
                    scanFinished()
                }
                ACTION_FOUND -> {
                    val device = intent.extras?.get(EXTRA_DEVICE) as BluetoothDevice ?: return
                    val name = intent.extras?.getString(EXTRA_NAME)
                    val rssi = intent.extras?.getShort(EXTRA_RSSI)?.toInt() ?: return

                    val bt = this@BluetoothPositionProvider
                    logger.debug("Discovered device $device ($name) ${rssi}db with $bt")
                    val address = device.address
                    val coordinate = deviceMap[address]

                    val signalStrength = SignalStrength(address, name, coordinate, rssi)

                    addDevice(signalStrength)
                }
            }
        }

    }

    private fun scanFinished() {
        logger.info("Finished BT discovery. Updating position estimate and scheduling new scan in ${minTimeBetweenUpdatesInMillis}s.")

        updatePositionEstimate()

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed({ triggerScan() }, minTimeBetweenUpdatesInMillis)
    }

    private fun addDevice(signalStrength: SignalStrength) {
        actualDevices[signalStrength.deviceId] = signalStrength

        updateLiveData()
        updatePositionEstimate()
    }

    private fun scanStarted() {
        logger.info("Started BT discovery. Clearing known devices...")
        actualDevices.clear()
        updateLiveData()
        updateLastScan()
    }

    private fun updateLiveData() {
        val bestBtDevices = actualDevices.values
            .sortedBy { -it.rssi }

        actualDevicesLiveData.value = bestBtDevices
    }

    override fun start() {
        if (enabled) return

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
        if (currentTime - lastUpdate < minTimeBetweenUpdatesInMillis) {
            logger.debug("Last update is too close.")
            return
        }

        val bestBtDevices = actualDevices.values

//        val bestDevicesAsString = bestBtDevices.joinToString("; ", prefix = "Best BTs: ") {
//            val device = it.device
//            "${device.address} ${device.name} ${it.rssi} ${calculateSignalLevel(it.rssi)}"
//        }
//        logger.info(bestDevicesAsString)


        val knownCoordinateDevices = bestBtDevices.filter { deviceMap.containsKey(it.deviceId) }
        val signalStrengths = knownCoordinateDevices
            .map {
                val coordinate = deviceMap[it.deviceId] ?: return@map null
                SignalStrength(it.deviceId, it.name, coordinate, it.rssi)
            }.filterNotNull()

        val nn = Trilateraion.naiveNN(signalStrengths)
        val newPosition = if (nn == null) null else IonavLocation(name, nn)

        logger.info("Updating last known position to $newPosition")
        lastKnownPosition = newPosition
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
        if (!enabled) return

        super.stop()

        bluetoothAdapter?.cancelDiscovery()
        try {
            context.unregisterReceiver(bcr)
        } catch (_: IllegalArgumentException) {/*not registered */
        }
    }


    companion object {
        const val numLevels = 10

        const val minTimeBetweenUpdatesInMillis = 5000L

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
        fun calculateSignalLevel(rssi: Int, numLevels: Int = BluetoothPositionProvider.numLevels): Int {
            return WifiManager.calculateSignalLevel(rssi, numLevels)
        }

        val BLUETOOTH_PROVIDER_NAME: String = BluetoothPositionProvider::class.java.simpleName
    }
}
