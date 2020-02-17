package de.ironjan.arionav.sample

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


class NearbyAccessPointsFragment : Fragment() {

    private lateinit var dataAdapter: MyAdapter
    private val model: NearbyAccessPointsViewModel by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_wifi_aps, container, false)
        val lContext = context ?: return view

        var viewManager = LinearLayoutManager(lContext)
        val nearbyAccessPoints = model.getNearbyAccessPoints()
        val myDataset = nearbyAccessPoints.value ?: emptyList()
        dataAdapter = MyAdapter(myDataset)

        view.findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = dataAdapter
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        registerLiveDataObservers(lifecycleOwner)

        Thread(Runnable {
            for (i in 1..10) {
                Thread.sleep(1000)
                model.foo()
            }
        }).start()

    }

    private fun registerLiveDataObservers(lifecycleOwner: LifecycleOwner) {
        model.getNearbyAccessPoints().observe(lifecycleOwner, Observer {
            dataAdapter.replaceData(it)
        })
    }
}