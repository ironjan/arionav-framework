package de.ironjan.arionav.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.whenCreated
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Either
import de.ironjan.arionav.ionav.GhzExtractor
import de.ironjan.arionav.ionav.special_routing.model.Poi
import de.ironjan.arionav.ionav.special_routing.model.Room
import de.ironjan.arionav.ionav.special_routing.repository.PoiRepository
import de.ironjan.arionav.ionav.special_routing.repository.RoomRepository
import de.ironjan.arionav.sample.ArionavSampleApplication.Companion.ghzResId
import de.ironjan.arionav.sample.ArionavSampleApplication.Companion.mapName
import de.ironjan.arionav.sample.viewmodel.EitherPoiRoomAdapter
import org.slf4j.LoggerFactory

class PoiListFragment : Fragment() {
    private var pois: Map<String, Poi> = emptyMap()
    private var rooms: Map<String, Room> = emptyMap()
    lateinit var dataAdapter: EitherPoiRoomAdapter

val logger = LoggerFactory.getLogger(PoiListFragment::class.java.name)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_wifi_aps, container, false)
        val lContext = context ?: return view

        val viewManager = LinearLayoutManager(lContext)
        val onItemClickListener = object : EitherPoiRoomAdapter.OnItemClickListener {
            override fun onItemClick(item: Either<Poi, Room>) {
                val coordinateStringOf = coordinateStringOf(item)
                var bundle = bundleOf("selectedPoiCoordinate" to coordinateStringOf)
                logger.info("Clicked on $coordinateStringOf, i.e. $item")
                findNavController().navigate(R.id.action_to_mapFragment, bundle)
            }

        }
        dataAdapter = EitherPoiRoomAdapter(emptyList(), onItemClickListener)

        view.findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = dataAdapter
        }

        return view
    }

    private fun coordinateStringOf(item: Either<Poi, Room>): String {
        return when (item) {
            is Either.Left -> item.a.coordinate.asString()
            is Either.Right -> item.b.coordinate.asString()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lContext = context ?: return
        Toast.makeText(lContext, "Loading...", Toast.LENGTH_SHORT).show()

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")

        val ghzExtractor = GhzExtractor(lContext.applicationContext, ghzResId, mapName)
        RoomRepository().getRooms(ghzExtractor.osmFilePath).observe(lifecycleOwner, Observer {
            rooms = it
            updateAdapter()
        })
        PoiRepository().getPois(ghzExtractor.osmFilePath).observe(lifecycleOwner, Observer {
            pois = it
            updateAdapter()
        })
        updateAdapter()
    }

    private fun updateAdapter() {
        val joined = rooms.map { Either.Right(it.value) } + pois.map { Either.Left(it.value) }
        dataAdapter.replaceData(joined)
    }

}