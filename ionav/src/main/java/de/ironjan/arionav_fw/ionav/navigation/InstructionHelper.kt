package de.ironjan.arionav_fw.ionav.navigation

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.ionav.R

class InstructionHelper(private val context: Context) {
    fun toText(currentInstruction: Instruction) = toText(currentInstruction, null)
    fun toText(currentInstruction: Instruction, nextInstruction: Instruction?): String {
        val instructionText = if(nextInstruction==null) "" else getTextFor(nextInstruction.sign)

        val timeInSeconds = currentInstruction.time / 1000
        val timeInMinutes = timeInSeconds / 60

        val distance = " %.2f".format(currentInstruction.distance)

        val optionalLessThan = if(timeInMinutes>0) "" else "<"
        return "$instructionText in ${distance}m ($optionalLessThan${timeInMinutes}min)\n${nextInstruction?.name}"
    }

    private fun getTextFor(sign: Int): String {
        // TODO improve
        return when (sign) {
            -99 -> "UNKNOWN"
            -98 -> "U_TURN_UNKNOWN"
            -8 -> "U_TURN_LEFT"
            -7 -> "KEEP_LEFT"
            -6 -> "LEAVE_ROUNDABOUT" // for future use
            -3 -> "TURN_SHARP_LEFT"
            -2 -> "TURN_LEFT"
            -1 -> "TURN_SLIGHT_LEFT"
            0 -> "CONTINUE_ON_STREET"
            1 -> "TURN_SLIGHT_RIGHT"
            2 -> "TURN_RIGHT"
            3 -> "TURN_SHARP_RIGHT"
            4 -> "FINISH"
            5 -> "REACHED_VIA"
            6 -> "USE_ROUNDABOUT"
            Integer.MIN_VALUE -> "IGNORE"
            7 -> "KEEP_RIGHT"
            8 -> "U_TURN_RIGHT"
            101 -> "PT_START_TRIP"
            102 -> "PT_TRANSFER"
            103 -> "PT_END_TRIP"
            else -> "UNKNOWN"
        }
    }

    fun getInstructionImageFor(sign: Int): Drawable? {
        // TODO add the others too? they were retrieved from graphhopper/./web/target/classes/assets/img/
        val resId = when (sign) {
            -99 -> R.mipmap.ic_launcher
            -98 -> R.mipmap.u_turn
            -8 -> R.mipmap.u_turn_left
            -7 -> R.mipmap.keep_left
//            -6 -> "LEAVE_ROUNDABOUT" // for future use
            -3 -> R.mipmap.sharp_left
            -2 -> R.mipmap.left
            -1 -> R.mipmap.slight_left
            0 -> R.mipmap.continue_in_direction
            1 -> R.mipmap.slight_right
            2 -> R.mipmap.right
            3 -> R.mipmap.sharp_right
//            4 -> "FINISH"
//            5 -> "REACHED_VIA"
            6 -> R.mipmap.roundabout
//            Integer.MIN_VALUE -> "IGNORE"
            7 -> R.mipmap.keep_right
            8 -> R.mipmap.u_turn_right
//            101 -> "PT_START_TRIP"
//            102 -> "PT_TRANSFER"
//            103 -> "PT_END_TRIP"
            else -> R.mipmap.ic_launcher
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.resources.getDrawable(resId, context.theme)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getDrawable(resId)
        }
    }

}