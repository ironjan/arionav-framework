package de.ironjan.arionav.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav.sample.viewmodel.MyAdapter

/* https://github.com/AltBeacon/android-beacon-library */
class NearbyBluetoothTokensFragment: NearbySendersListFragment<String>({it}) {

}