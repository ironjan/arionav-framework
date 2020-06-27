package de.ironjan.arionav_fw.samples.campus.views

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.positioning.IPositionProvider
import de.ironjan.arionav_fw.samples.campus.CampusSampleApplication
import de.ironjan.arionav_fw.samples.campus.R
import de.ironjan.arionav_fw.samples.campus.adapters.ProvidersAdapter
import de.ironjan.arionav_fw.samples.campus.util.PreferenceKeys
import kotlinx.android.synthetic.main.fragment_recycler_view.*

class PositioningProviderConfigFragment : Fragment(R.layout.fragment_recycler_view) {

    private lateinit var providersAdapter: ProvidersAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Positioning Config"

        val positioningService = when (val ionavHolder = activity?.application) {
            is IonavContainerHolder -> ionavHolder.ionavContainer.positioningService
            else -> null
        } ?: return

        val lifecycleOwner = viewLifecycleOwner

        val onCheckboxClickCallback = object : ProvidersAdapter.OnCheckboxClickCallback {
            override fun onClick(iPositionProvider: IPositionProvider, newState: Boolean) {
                if(newState == iPositionProvider.enabled) return

                if (newState) {
                    positioningService.enableProvider(iPositionProvider)
                }else {
                    positioningService.disableProvider(iPositionProvider)
                }

                updatePreferences()
            }

        }
        providersAdapter = ProvidersAdapter(lifecycleOwner, positioningService, onCheckboxClickCallback)


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
                updatePreferences()
                return true
            }
        })
        itemTouchHelper.attachToRecyclerView(recycler_view)
    }

    override fun onPause() {
        super.onPause()

        updatePreferences()
    }

    private fun updatePreferences() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val edit = sharedPref.edit()

        val positioningService = (activity?.application as CampusSampleApplication).ionavContainer.positioningService ?: return


        val providersList = positioningService.providers.value ?: return
        providersList.mapIndexed { priority, provider ->
            provider.name + provider.enabled
            val enabledKey = PreferenceKeys.enabledKey(provider.name)
            val priorityKey = PreferenceKeys.priorityKey(provider.name)

            edit.putBoolean(enabledKey, provider.enabled)
            edit.putInt(priorityKey, priority)
        }
        edit.apply()
    }
}

