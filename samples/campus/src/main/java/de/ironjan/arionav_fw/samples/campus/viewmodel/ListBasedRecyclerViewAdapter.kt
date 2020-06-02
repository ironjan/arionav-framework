package de.ironjan.arionav_fw.samples.campus.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav_fw.samples.campus.R

class ListBasedRecyclerViewAdapter<T>(private var myDataset: List<T>, private val toStringConverter: (T) -> String) :
    RecyclerView.Adapter<ListBasedRecyclerViewAdapter.ViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ListBasedRecyclerViewAdapter.ViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_text_view, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element


        holder.textView.text = toStringConverter(myDataset[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    fun replaceData(it: List<T>) {
        myDataset = it
        notifyDataSetChanged()
    }

    fun swap(pos1: Int, pos2: Int) {
        val tmpCopyDataset = myDataset.toMutableList()

        val tmp = tmpCopyDataset[pos1]
        tmpCopyDataset[pos1] = tmpCopyDataset[pos2]
        tmpCopyDataset[pos2] = tmp

        replaceData(tmpCopyDataset)
    }
}

