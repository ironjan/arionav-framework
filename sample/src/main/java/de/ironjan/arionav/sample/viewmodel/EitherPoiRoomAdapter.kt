package de.ironjan.arionav.sample.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import arrow.core.Either
import de.ironjan.arionav.ionav.special_routing.model.Poi
import de.ironjan.arionav.ionav.special_routing.model.Room
import de.ironjan.arionav.sample.R


class EitherPoiRoomAdapter(
    private var myDataset: List<Either<Poi, Room>>,
    private val onItemClickListener: EitherPoiRoomAdapter.OnItemClickListener
) : RecyclerView.Adapter<EitherPoiRoomAdapter.EitherPoiRoomAdapterViewHolder>() {
    class EitherPoiRoomAdapterViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    interface OnItemClickListener {
        fun onItemClick(item: Either<Poi, Room>)
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
        val either = myDataset[position]
        val tv = holder.textView
        tv.text =
            when (either) {
                is Either.Left -> either.a.name
                is Either.Right -> either.b.name
            }
        tv.setOnClickListener { onItemClickListener.onItemClick(either) }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    fun replaceData(it: List<Either<Poi, Room>>) {
        myDataset = it
        notifyDataSetChanged()
    }
}