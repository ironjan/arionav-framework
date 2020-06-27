package de.ironjan.arionav_fw.samples.campus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav_fw.samples.campus.adapters.ListBasedRecyclerViewAdapter

abstract class CustomListFragment<T>(private val toStringConverter: (T) -> String) : Fragment() {
    lateinit var dataAdapter: ListBasedRecyclerViewAdapter<T>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_list, container, false)
        val lContext = context ?: return view

        val viewManager = LinearLayoutManager(lContext)
        dataAdapter = ListBasedRecyclerViewAdapter(emptyList()) { toStringConverter(it) }

        view.findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = dataAdapter
        }

        return view
    }


    protected fun replaceData(results: List<T>) {
        dataAdapter.replaceData(results)
    }
}