package de.ironjan.arionav.sample

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.view.View
import de.ironjan.arionav.ionav.positioning.bluetooth.BluetoothLeSpike
import org.altbeacon.beacon.*
import org.slf4j.LoggerFactory
import kotlin.math.log


/* https://github.com/AltBeacon/android-beacon-library */
class NearbyBluetoothTokensFragment : NearbySendersListFragment<String>({ it }), BeaconConsumer {
    private val logger = LoggerFactory.getLogger(NearbyBluetoothTokensFragment::class.java.simpleName)
    override fun getApplicationContext(): Context {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unbindService(p0: ServiceConnection?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bindService(p0: Intent?, p1: ServiceConnection?, p2: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBeaconServiceConnect() {
        beaconManager?.apply {
            removeAllMonitorNotifiers()
            addMonitorNotifier(object : MonitorNotifier {
                override fun didEnterRegion(region: Region) {
                    logger.info("I just saw an beacon for the first time! ${region.bluetoothAddress}")
                }

                override fun didExitRegion(region: Region) {
                    logger.info("I no longer see an beacon: ${region.bluetoothAddress}")
                }

                override fun didDetermineStateForRegion(state: Int, region: Region) {
                    logger.info("I have just switched from seeing/not seeing beacons: $state")
                }
            })
            addRangeNotifier { beacons, region ->
                if (beacons.isNotEmpty()) {
                    logger.info("The first beacon I see is about " + beacons.iterator().next().distance + " meters away.")
                }
            }

        }

        try {
            beaconManager?.startMonitoringBeaconsInRegion(Region("myMonitoringUniqueId", null, null, null))
        } catch (e: RemoteException) {
            logger.error(e.message, e)
        }

    }

    private var beaconManager: BeaconManager? = null

    private val devices = emptyMap<String, String>().toMutableMap()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lContext = context ?: return
        val cb = object: BluetoothAdapter.LeScanCallback {
            val numLevels = 10
            override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
                logger.info("onLeScan: $device, $rssi, $scanRecord")
                if(device==null) return
                val address = device.address
                val strength = calculateSignalLevel(rssi, numLevels)
                val s = "$address ${device.name} $rssi , $strength/$numLevels"

                devices[address] = s
                dataAdapter.replaceData(devices.values.toList())
            }

        }
         val handler: Handler = Handler(Looper.getMainLooper())

        val bluetoothLeSpike = BluetoothLeSpike(lContext, cb, handler)
        bluetoothLeSpike.scanLeDevice(true)
        logger.info("Triggered scan")
    }


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
    fun calculateSignalLevel(rssi: Int, numLevels: Int): Int {
        val MIN_RSSI = -100
        val MAX_RSSI = -30 // by test with big tokens right beside phone
        if (rssi <= MIN_RSSI) {
            return 0
        } else if (rssi >= MAX_RSSI) {
            return numLevels - 1
        } else {
            val inputRange = (MAX_RSSI - MIN_RSSI).toFloat()
            val outputRange = (numLevels - 1).toFloat()
            return ((rssi - MIN_RSSI).toFloat() * outputRange / inputRange).toInt()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        beaconManager?.unbind(this);
    }

    companion object {
    }

}