package de.ironjan.arionav.sample.util

import android.content.Context
import android.graphics.drawable.Drawable
import com.graphhopper.util.Instruction
import de.ironjan.arionav.sample.R
import kotlin.math.round

object InstructionHelper {
    fun toText(instruction: Instruction): String {
        val distance = round(instruction.distance * 100) / 100
        val instructionText = InstructionHelper.getTextFor(instruction.sign)
        val timeInSeconds = instruction.time / 1000
        val timeInMinutes = timeInSeconds / 60
        return "$instructionText ${instruction.name}, ${distance}m, ${timeInMinutes}min"
    }

    private fun getTextFor(sign: Int): String {
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

    fun getInstructionImageFor(sign: Int, context: Context): Drawable? {
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
        return context.resources.getDrawable(resId, context.theme)
    }
}