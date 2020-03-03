package de.ironjan.arionav.ionav.positioning.config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav.ionav.R
import de.ironjan.arionav.ionav.positioning.PositioningProviderRegistry
import kotlinx.android.synthetic.main.fragment_provider_configuration.*

class ProviderConfigFragment : Fragment() {

    private lateinit var providersAdapter: ProvidersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_provider_configuration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registry = PositioningProviderRegistry.Instance

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        providersAdapter = ProvidersAdapter(lifecycleOwner)


        val context = context ?: return
        providerRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter=providersAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                /* do nothing */
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                registry.swapPriorities(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }
        })
        itemTouchHelper.attachToRecyclerView(providerRecyclerView)


    }
}

