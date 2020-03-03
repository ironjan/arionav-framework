package de.ironjan.arionav.ionav.positioning.config

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.ironjan.arionav.ionav.R
import de.ironjan.arionav.ionav.positioning.IPositionProvider
import de.ironjan.arionav.ionav.positioning.PositioningProviderRegistry
import kotlinx.android.synthetic.main.view_fragment_config_list_item.view.*

class ProvidersAdapter(private val lifecycleOwner: LifecycleOwner) :
    RecyclerView.Adapter<ProvidersAdapter.MyViewHolder>() {
    private var displayedData: List<IPositionProvider> = listOf()
    private val providerRegistry = PositioningProviderRegistry.Instance

    init {
        providerRegistry.providers.observe(lifecycleOwner, Observer {
            displayedData = it
            notifyDataSetChanged()
        })
    }

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_fragment_config_list_item, parent, false)

        return MyViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return displayedData.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val iPositionProvider = displayedData[position]
        holder.view.txt_name.text = iPositionProvider.name
        val checkboxEnabled = holder.view.checkbox_enabled
        checkboxEnabled.isChecked = iPositionProvider.enabled
        checkboxEnabled.setOnClickListener {
            if (it.isEnabled) {
                if(!iPositionProvider.enabled) {
                    iPositionProvider.start()
                }
            } else if(iPositionProvider.enabled) {
                    iPositionProvider.stop()
            }
        }
    }

}