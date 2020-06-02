package de.ironjan.arionav_fw.samples.tourism

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.ar.sceneform.rendering.ViewRenderable
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.arionav.views.ArNavFragment
import de.ironjan.arionav_fw.ionav.services.InstructionHelper


class CustomArNavFragment : ArNavFragment() {
    override val instructionLayoutId = R.layout.view_custom_instruction

    override fun updateRenderable(renderable: ViewRenderable,
                                  currentInstruction: Instruction,
                                  nextInstruction: Instruction?) {

        val txtInstruction = renderable.view.findViewById<TextView>(de.ironjan.arionav_fw.arionav.R.id.instructionText)
        val instructionImage = renderable.view.findViewById<ImageView>(de.ironjan.arionav_fw.arionav.R.id.instructionImage)

        val instructionText = instructionHelper.toText(currentInstruction, nextInstruction)
        txtInstruction.text = instructionText
        txtInstruction.visibility = if (instructionText.isNullOrBlank()) View.GONE else View.VISIBLE

        val sign = if(currentInstruction.points.size > 2) {
            currentInstruction.sign
        }else {
            nextInstruction?.sign  ?: InstructionHelper.SIGN_DESTINATION
        }
        instructionImage.setImageDrawable(instructionHelper.getInstructionImageFor(sign))
    }
}