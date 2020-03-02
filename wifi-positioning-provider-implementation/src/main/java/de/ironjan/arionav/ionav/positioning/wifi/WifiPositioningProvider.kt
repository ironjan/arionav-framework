package de.ironjan.arionav.ionav.positioning.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.calculateSignalLevel
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav.ionav.positioning.IonavLocation
import de.ironjan.arionav.ionav.positioning.PositionProviderBaseImplementation
import de.ironjan.arionav.ionav.positioning.SignalStrength
import de.ironjan.arionav.ionav.positioning.Trilateraion.naiveTrilateration
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

class WifiPositioningProvider(private val context: Context, private val lifecycle: Lifecycle) : PositionProviderBaseImplementation(context, lifecycle) {
    private val lastScan: MutableLiveData<String> = MutableLiveData("")
    fun getLastScan(): LiveData<String> = lastScan

    override val name: String = WifiPositioningProvider::class.java.simpleName

    private val logger = LoggerFactory.getLogger(WifiPositioningProvider::class.java.simpleName)

    private val devices: MutableMap<String, ScanResult> = mutableMapOf()


    private val listOfVisibleDevices: MutableLiveData<List<ScanResult>> = MutableLiveData(listOf())
    fun getVisibleDevices(): LiveData<List<ScanResult>> = listOfVisibleDevices


    private val tmpIdToCoordinate: Map<String, Coordinate> = WifiPositioningProviderHardCodedValues.macsToCoordinates

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

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        triggerScan()
    }

    private fun triggerScan() {
        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            scanFailure()
        }
    }


    private fun scanSuccess() {
        val sortedResults = wifiManager.scanResults.sortedBy { -it.level }

        listOfVisibleDevices.value = sortedResults

        updateLastScan()

        updatePositionEstimate()

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed({ triggerScan() }, 10000)
    }

    private fun updateLastScan() {
        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz)
        val nowAsISO = df.format(Date())
        lastScan.value = nowAsISO
    }


    private fun updatePositionEstimate() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate < minTimeBetweenUpdatesInMillis)
            return

        lastUpdate = currentTime


        val bestBtDevices = listOfVisibleDevices.value!!

        val bestDevicesAsString = bestBtDevices.joinToString("; ", prefix = "Best BTs: ") {
            val device = it
            val calculateSignalLevel = calculateSignalLevel(it.level, 10)
            "${device.BSSID} ${device.SSID} ${it.level} $calculateSignalLevel"
        }
        logger.info(bestDevicesAsString)


        val knownCoordinateDevices = bestBtDevices.filter { tmpIdToCoordinate.containsKey(it.BSSID) }
        val signalStrengths = knownCoordinateDevices
            .map { SignalStrength(it.BSSID, tmpIdToCoordinate[it.BSSID]!!, it.level) }

        val coordinate = naiveTrilateration(signalStrengths)
        lastKnownPosition = if (coordinate == null) null else IonavLocation(name, coordinate)
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
