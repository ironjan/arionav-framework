package de.ironjan.arionav_fw.arionav.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.arionav.R
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import kotlinx.android.synthetic.main.fragment_ar_view.*

open class ArNavFragment : Fragment() {
    // region view model
    open val model: IonavViewModel by activityViewModels()
    // endregion

    // region lifecycle events
    protected lateinit var instructionHelper: InstructionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instructionHelper = InstructionHelper(context ?: return)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_ar_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ar_route_view.setInstructionView(instructionLayoutId, this::updateInstructionView)
        ar_route_view.observe(model, viewLifecycleOwner)
    }
    // endregion

    // region ar instruction view
    protected open val instructionLayoutId = R.layout.view_basic_instruction

    protected open fun updateInstructionView(view: View, currentInstruction: Instruction, nextInstruction: Instruction?) {
        val txtName = view.findViewById<TextView>(R.id.instructionText)
        val txtDistance = view.findViewById<TextView>(R.id.instructionDistanceInMeters)
        val instructionImage = view.findViewById<ImageView>(R.id.instructionImage)

        txtName.text = currentInstruction.name
        txtDistance.text = "%.2fm".format(currentInstruction.distance)
        instructionImage.setImageDrawable(instructionHelper.getInstructionImageFor(currentInstruction.sign))
    }
    // endregion
}
