package de.ironjan.arionav.sample

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.RemoteException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav.sample.viewmodel.MyAdapter
import org.altbeacon.beacon.*
import org.slf4j.LoggerFactory


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lContext = context ?: return
        beaconManager = BeaconManager.getInstanceForApplication(lContext);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
         beaconManager?.apply {
             beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
             beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT));
             beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
             beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
             beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
             bind(this@NearbyBluetoothTokensFragment)
         }
    }


    override fun onDestroy() {
        super.onDestroy()
        beaconManager?.unbind(this);
    }

}