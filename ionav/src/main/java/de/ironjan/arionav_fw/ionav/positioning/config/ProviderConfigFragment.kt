package de.ironjan.arionav_fw.ionav.positioning.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav_fw.ionav.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.R
import kotlinx.android.synthetic.main.fragment_with_recycler_view.*

class ProviderConfigFragment : Fragment() {

    private lateinit var providersAdapter: ProvidersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_with_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val positioningService = when (val ionavHolder = activity?.application) {
            is IonavContainerHolder -> ionavHolder.ionavContainer.positioningService
            else -> null
        } ?: return

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        providersAdapter = ProvidersAdapter(lifecycleOwner, positioningService)


        val context = context ?: return
        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = providersAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                /* do nothing */
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                positioningService.swapPriorities(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }
        })
        itemTouchHelper.attachToRecyclerView(recycler_view)


    }
}

