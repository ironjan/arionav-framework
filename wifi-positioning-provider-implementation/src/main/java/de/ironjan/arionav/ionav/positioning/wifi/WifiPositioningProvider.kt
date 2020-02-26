package de.ironjan.arionav.ionav.positioning.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.calculateSignalLevel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.positioning.IonavLocation
import de.ironjan.arionav.ionav.positioning.PositionProviderBaseImplementation
import de.ironjan.arionav.ionav.positioning.SignalStrength
import de.ironjan.arionav.ionav.positioning.Trilateraion.naiveTrilateration
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory

class WifiPositioningProvider(private val context: Context, private val lifecycle: Lifecycle) : PositionProviderBaseImplementation(context, lifecycle) {
    override val name: String = WifiPositioningProvider::class.java.simpleName

    private val logger = LoggerFactory.getLogger(WifiPositioningProvider::class.java.simpleName)

    private val devices: MutableMap<String, ScanResult> = mutableMapOf()


    private val listOfVisibleBtDevices: MutableLiveData<List<ScanResult>> = MutableLiveData(listOf())
    fun getVisibleBluetoothDevices(): LiveData<List<ScanResult>> = listOfVisibleBtDevices

    private val tmpIdToCoordinate: Map<String, Coordinate> = WifiPositioningProviderHardCodedValues().macsToCoordinates

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiScanReceiver: BroadcastReceiver

    override fun start() {
        wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }
        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            scanFailure()
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

    }


    private fun scanSuccess() {
        val sortedResults = wifiManager.scanResults.sortedBy { - it.level }

        listOfVisibleBtDevices.value = sortedResults

        updatePositionEstimate()
    }


    private fun updatePositionEstimate() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate < minTimeBetweenUpdatesInMillis)
            return

        lastUpdate = currentTime


        val bestBtDevices = listOfVisibleBtDevices.value!!

        val bestDevicesAsString = bestBtDevices.joinToString("; ", prefix = "Best BTs: ") {
            val device = it
            val calculateSignalLevel = calculateSignalLevel(it.level,10)
            "${device.BSSID} ${device.SSID} ${it.level} $calculateSignalLevel"
        }
        logger.info(bestDevicesAsString)


        val knownCoordinateDevices = bestBtDevices.filter { tmpIdToCoordinate.containsKey(it.BSSID) }
        val signalStrengths = knownCoordinateDevices
            .map { SignalStrength(it.BSSID, tmpIdToCoordinate[it.BSSID]!!, it.level) }

        val coordinate = naiveTrilateration(signalStrengths)
        lastKnownPosition = if(coordinate == null) null else IonavLocation(name, coordinate)
    }

    private fun scanFailure() {
        // todo
    }

    override fun stop() {
        context.unregisterReceiver(wifiScanReceiver)
    }

    companion object {
        const val numLevels = 10

        const val minTimeBetweenUpdatesInMillis = 1000

    }
}
