package de.ironjan.arionav_fw.ionav.views.debug

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.graphhopper.PathWrapper
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel

class TextInstructionAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: IonavViewModel
) : RecyclerView.Adapter<TextInstructionAdapter.TextualInstructionViewHolder>() {

    class TextualInstructionViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private var displayedData = listOf<Instruction>()

    private val instructionHelper by lazy { viewModel.instructionHelper }

    init {
        viewModel.route.observe(lifecycleOwner, Observer {
            showInstructionsOf(it)
        })
        showInstructionsOf(viewModel.route.value)
    }

    private fun showInstructionsOf(route: PathWrapper?) {
        displayedData = route?.instructions?.toList() ?: emptyList<Instruction>()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextualInstructionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_list_item_instruction, parent, false)
        return TextualInstructionViewHolder(view)
    }

    override fun getItemCount(): Int = displayedData.size

    override fun onBindViewHolder(holder: TextualInstructionViewHolder, position: Int) {
        val currentInstruction = displayedData[position]

        holder.view.findViewById<AppCompatImageView>(R.id.imgInstruction).setImageResource(instructionHelper.getDrawableResIdForInstructionSign(currentInstruction))
        holder.view.findViewById<TextView>(R.id.txtInstruction).text = instructionHelper.getTextFor(currentInstruction)
        holder.view.findViewById<TextView>(R.id.txtDistance).text = InstructionHelper.toReadableDistance(currentInstruction.distance)
        holder.view.findViewById<TextView>(R.id.txtDuration).text = InstructionHelper.toReadableTime(currentInstruction.time)
    }


}