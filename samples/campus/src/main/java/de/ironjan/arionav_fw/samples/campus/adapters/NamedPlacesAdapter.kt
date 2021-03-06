package de.ironjan.arionav_fw.samples.campus.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav_fw.samples.campus.R


class NamedPlacesAdapter(
    private var myDataset: List<String>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<NamedPlacesAdapter.EitherPoiRoomAdapterViewHolder>() {
    class EitherPoiRoomAdapterViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    interface OnItemClickListener {
        fun onItemClick(placeName: String)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EitherPoiRoomAdapterViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_text_view, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters
        return EitherPoiRoomAdapterViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: EitherPoiRoomAdapterViewHolder, position: Int) {
        val place = myDataset[position]
        val tv = holder.textView
        tv.text = place
        tv.setOnClickListener { onItemClickListener.onItemClick(place) }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    fun replaceData(it: List<String>) {
        myDataset = it.sorted()
        notifyDataSetChanged()
    }
}