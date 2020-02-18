package de.ironjan.arionav.sample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav.ionav.positioning.wifi.model.SignalStrengthResult
import de.ironjan.arionav.sample.viewmodel.NearbyAccessPointsViewModel


class NearbyAccessPointsFragment : CustomListFragment<SignalStrengthResult>({ scanResult -> "${scanResult.BSSID} ${scanResult.level} ${scanResult.scanResult.level}dbm" }) {

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiScanReceiver: BroadcastReceiver
    private val model: NearbyAccessPointsViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanSuccess()

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        registerLiveDataObservers(lifecycleOwner)

    }

    private fun registerLiveDataObservers(lifecycleOwner: LifecycleOwner) {
        model.getNearbyAccessPoints().observe(lifecycleOwner, Observer {
            //            dataAdapter.replaceData(it)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWifiThings()
    }

    private fun setupWifiThings() {
        val lContext = context ?: return
        wifiManager = lContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
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
    }

    private fun scanSuccess() {
        updateData(toSignalStrengthResults(wifiManager.scanResults))
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        // TODO do nothing instead?
        updateData(toSignalStrengthResults(wifiManager.scanResults))
    }

    private fun toSignalStrengthResults(results: List<ScanResult>): List<SignalStrengthResult> {
        return results.map {
            val lvl = WifiManager.calculateSignalLevel(it.level, SignalStrengthResult.maxLevel)
            SignalStrengthResult(it, lvl)
        }
    }

    override fun onResume() {
        super.onResume()
        val lContext = context ?: return
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        lContext.registerReceiver(wifiScanReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        val lContext = context ?: return
        lContext.unregisterReceiver(wifiScanReceiver)
    }
}