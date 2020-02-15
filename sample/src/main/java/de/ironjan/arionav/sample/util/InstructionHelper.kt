package de.ironjan.arionav.sample.util

import com.graphhopper.util.Instruction
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
}