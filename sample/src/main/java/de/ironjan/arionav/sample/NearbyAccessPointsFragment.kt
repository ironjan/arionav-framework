package de.ironjan.arionav.sample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import de.ironjan.arionav.sample.viewmodel.MyAdapter
import de.ironjan.arionav.sample.viewmodel.NearbyAccessPointsViewModel
import java.lang.IllegalArgumentException
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav.ionav.positioning.wifi.model.SignalStrengthResult


class NearbyAccessPointsFragment : Fragment() {

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiScanReceiver: BroadcastReceiver
    private lateinit var dataAdapter: MyAdapter
    private val model: NearbyAccessPointsViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_wifi_aps, container, false)
        val lContext = context ?: return view

        var viewManager = LinearLayoutManager(lContext)
        val nearbyAccessPoints = model.getNearbyAccessPoints()
        dataAdapter = MyAdapter(emptyList())

        view.findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = dataAdapter
        }
        scanSuccess()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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
        updateData(wifiManager.scanResults)
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        // TODO do nothing instead?
        updateData(wifiManager.scanResults)
    }

    private fun updateData(results: MutableList<ScanResult>) {


        dataAdapter.replaceData( results.map {
            val lvl  = WifiManager.calculateSignalLevel(it.level, SignalStrengthResult.maxLevel)
            SignalStrengthResult(it, lvl) })
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