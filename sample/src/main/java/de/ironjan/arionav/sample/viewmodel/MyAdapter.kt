package de.ironjan.arionav.sample.viewmodel

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav.ionav.positioning.wifi.model.SignalStrengthResult
import de.ironjan.arionav.sample.R

class MyAdapter(private var myDataset: List<SignalStrengthResult>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_text_view, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val scanResult = myDataset[position]


        holder.textView.text = "${scanResult.BSSID} ${scanResult.level} ${scanResult.scanResult.level}dbm"
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    fun replaceData(it: List<SignalStrengthResult>) {
        myDataset = it
        notifyDataSetChanged()
    }
}

