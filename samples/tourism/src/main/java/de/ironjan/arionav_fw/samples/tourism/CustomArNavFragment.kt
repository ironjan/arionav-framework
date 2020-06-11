package de.ironjan.arionav_fw.samples.tourism

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.arionav.views.ArNavFragment
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import de.ironjan.arionav_fw.samples.tourism.viewmodel.TourismViewModel


class CustomArNavFragment : ArNavFragment() {
    override val model: TourismViewModel by activityViewModels()

    override val instructionLayoutId = R.layout.view_custom_instruction

    override fun updateInstructionView(view: View,
                                       currentInstruction: Instruction,
                                       nextInstruction: Instruction) {



        val txtInstruction = view.findViewById<TextView>(de.ironjan.arionav_fw.arionav.R.id.instructionText)
        val instructionText = instructionHelper.getTextFor(nextInstruction)
        txtInstruction.text = instructionText
        txtInstruction.visibility = if (instructionText.isBlank()) View.GONE else View.VISIBLE

        val instructionImage = view.findViewById<ImageView>(de.ironjan.arionav_fw.arionav.R.id.instructionImage)
        instructionImage.setImageDrawable(instructionHelper.getInstructionImageFor(nextInstruction.sign))

        view.findViewById<TextView>(R.id.txtDistance).text = InstructionHelper.toReadableDistance(currentInstruction.distance)
    }
}