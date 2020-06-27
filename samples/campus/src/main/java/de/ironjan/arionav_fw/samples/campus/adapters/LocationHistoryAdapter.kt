package de.ironjan.arionav_fw.samples.campus.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.samples.campus.viewmodel.DebugFragmentsViewModel
import java.text.SimpleDateFormat
import java.util.*

class LocationHistoryAdapter(private val lifecycleOwner: LifecycleOwner,
                             private val viewModel: DebugFragmentsViewModel)
    : RecyclerView.Adapter<LocationHistoryAdapter.LocationHistoryViewHolder>(){

    class LocationHistoryViewHolder(val view: View): RecyclerView.ViewHolder(view)

    private var displayedData = listOf<IonavLocation>()

    init {
        viewModel.locationHistory.observe(lifecycleOwner, Observer {
            displayedData = it
            notifyDataSetChanged()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return LocationHistoryViewHolder(view)
    }

    override fun getItemCount(): Int = displayedData.size

    override fun onBindViewHolder(holder: LocationHistoryViewHolder, position: Int) {
        val location = displayedData[position]
        val timestamp = location.timestamp

        val tz = TimeZone.getDefault()
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz)
        val isoTimestamp = df.format(timestamp)


        val shortenedProvider = location.provider.split("Position")[0]

        val text = "$shortenedProvider: ${location.coordinate.asString()}\n$isoTimestamp"
        holder.view.findViewById<TextView>(android.R.id.text1).text = text
    }


}