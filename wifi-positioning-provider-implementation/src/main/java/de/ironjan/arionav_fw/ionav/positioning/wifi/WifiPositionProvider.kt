package de.ironjan.arionav_fw.ionav.positioning.wifi

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
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.positioning.PositionProviderBaseImplementation
import de.ironjan.arionav_fw.ionav.positioning.SignalStrength
import de.ironjan.arionav_fw.ionav.positioning.Trilateraion.naiveNN
import de.ironjan.arionav_fw.ionav.positioning.wifi.WifiPositioningProviderHardCodedValues.macsToRooms
import de.ironjan.graphhopper.extensions_core.Coordinate
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

class WifiPositionProvider(private val context: Context,
                           private val lifecycle: Lifecycle,
                           private val deviceMap: Map<String, Coordinate>) : PositionProviderBaseImplementation(context, lifecycle) {
    private val lastScan: MutableLiveData<String> = MutableLiveData("")
    fun getLastScan(): LiveData<String> = lastScan

    override val name: String = WIFI_POSITIONING_PROVIDER

    private val logger = LoggerFactory.getLogger(WifiPositionProvider::class.java.simpleName)

    private val devices: MutableMap<String, ScanResult> = mutableMapOf()


    private val listOfVisibleDevices: MutableLiveData<List<ScanResult>> = MutableLiveData(listOf())
    fun getVisibleDevices(): LiveData<List<ScanResult>> = listOfVisibleDevices


    private lateinit var wifiManager: WifiManager
    private lateinit var wifiScanReceiver: BroadcastReceiver

    override fun start() {
        if(enabled) return

        super.start()

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

    private fun triggerScan(delay: Long = 0) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed({
            val success = wifiManager.startScan()
            if (!success) {
                // scan failure handling
                scanFailure()
            }
        }, delay)
    }


    private fun scanSuccess() {
        val sortedResults = wifiManager.scanResults.sortedBy { -it.level }

        listOfVisibleDevices.value = sortedResults

        updateLastScan()

        updatePositionEstimate()

        triggerScan(30000)
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

        lastUpdate = currentTime


        val bestBtDevices = listOfVisibleDevices.value!!

        val bestDevicesAsString = bestBtDevices.joinToString("; ", prefix = "Best APs: ") {
            val device = it
            val calculateSignalLevel = calculateSignalLevel(it.level, 10)
            "${device.BSSID} ${device.SSID} ${it.level} $calculateSignalLevel"
        }
        logger.info(bestDevicesAsString)


        val knownCoordinateDevices = bestBtDevices.filter { deviceMap.containsKey(it.BSSID) }
        val signalStrengths = knownCoordinateDevices
            .map { SignalStrength(it.BSSID, macsToRooms[it.BSSID], deviceMap[it.BSSID]!!, it.level) }

        val coordinate = naiveNN(signalStrengths)

        val newLocation = if (coordinate == null) null else IonavLocation(name, coordinate)

        logger.info("Updating last known location to $newLocation")

        lastKnownPosition = newLocation
    }

    private fun scanFailure() {
        logger.warn("ScanFailure in WifiPositioning provider. Scheduling new scan in 1m.")
        triggerScan(60000)
    }

    override fun stop() {
        if(!enabled) return

        super.stop()

        try{context.unregisterReceiver(wifiScanReceiver)}catch (_: IllegalArgumentException) {/*not registered */}
    }

    companion object {
        const val numLevels = 10

        const val minTimeBetweenUpdatesInMillis = 1000

        val WIFI_POSITIONING_PROVIDER = WifiPositionProvider::class.java.simpleName
    }
}
