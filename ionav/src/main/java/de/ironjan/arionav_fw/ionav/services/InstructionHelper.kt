package de.ironjan.arionav_fw.ionav.services

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.ionav.R
import java.util.*

class InstructionHelper(private val context: Context) {
    fun toText(currentInstruction: Instruction): String {
        val textFor = getTextFor(currentInstruction.sign)
        val toReadableDistance = toReadableDistance(currentInstruction.distance)
        return "$textFor in $toReadableDistance"
    }

    fun toText(currentInstruction: Instruction, nextInstruction: Instruction?): String {
        val instructionText = if (nextInstruction == null) context.resources.getString(R.string.instruction_finish) else getTextFor(nextInstruction.sign)


        val distance = toReadableDistance(currentInstruction.distance)

        return "$instructionText in $distance"
    }

    fun getTextFor(instruction: Instruction?) = getTextFor(instruction?.sign ?: 4)
    fun getTextFor(sign: Int): String {
        return when (sign) {
            Instruction.UNKNOWN -> context.resources.getString(R.string.instruction_unknown)
            Instruction.U_TURN_UNKNOWN -> context.resources.getString(R.string.instruction_u_turn_unknown)
            Instruction.U_TURN_LEFT -> context.resources.getString(R.string.instruction_u_turn_left)
            Instruction.KEEP_LEFT -> context.resources.getString(R.string.instruction_keep_left)
            Instruction.LEAVE_ROUNDABOUT -> context.resources.getString(R.string.instruction_leave_roundabout)
            Instruction.TURN_SHARP_LEFT -> context.resources.getString(R.string.instruction_turn_sharp_left)
            Instruction.TURN_LEFT -> context.resources.getString(R.string.instruction_turn_left)
            Instruction.TURN_SLIGHT_LEFT -> context.resources.getString(R.string.instruction_turn_slight_left)
            Instruction.CONTINUE_ON_STREET-> context.resources.getString(R.string.instruction_continue_on_street)
            Instruction.TURN_SLIGHT_RIGHT -> context.resources.getString(R.string.instruction_turn_slight_right)
            Instruction.TURN_RIGHT -> context.resources.getString(R.string.instruction_turn_right)
            Instruction.TURN_SHARP_RIGHT -> context.resources.getString(R.string.instruction_turn_sharp_right)
            Instruction.FINISH -> context.resources.getString(R.string.instruction_finish)
            Instruction.REACHED_VIA -> context.resources.getString(R.string.instruction_reached_via)
            Instruction.USE_ROUNDABOUT -> context.resources.getString(R.string.instruction_use_roundabout)
            Instruction.IGNORE -> "IGNORED"
            Instruction.KEEP_RIGHT -> context.resources.getString(R.string.instruction_keep_right)
            Instruction.U_TURN_RIGHT -> context.resources.getString(R.string.instruction_u_turn_right)
            else -> context.resources.getString(R.string.instruction_unknown)
        }
    }

    fun getInstructionImageFor(instruction: Instruction) = getInstructionImageFor(instruction.sign)
    fun getInstructionImageFor(sign: Int?): Drawable? {
        val resId = getDrawableResIdForInstructionSign(sign)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.resources.getDrawable(resId, context.theme)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getDrawable(resId)
        }
    }

    // TODO add the others too? they were retrieved from graphhopper/./web/target/classes/assets/img/

    fun getDrawableResIdForInstructionSign(instruction: Instruction?) = getDrawableResIdForInstructionSign(instruction?.sign ?: 4)
    fun getDrawableResIdForInstructionSign(sign: Int?): Int = when (sign) {
        Instruction.UNKNOWN -> R.mipmap.ic_launcher
        Instruction.U_TURN_UNKNOWN -> R.mipmap.u_turn
        Instruction.U_TURN_LEFT -> R.mipmap.u_turn_left
        Instruction.KEEP_LEFT -> R.mipmap.keep_left
        Instruction.TURN_SHARP_LEFT -> R.mipmap.sharp_left
        Instruction.TURN_LEFT -> R.mipmap.left
        Instruction.TURN_SLIGHT_LEFT -> R.mipmap.slight_left
        Instruction.CONTINUE_ON_STREET -> R.mipmap.continue_in_direction
        Instruction.TURN_SLIGHT_RIGHT -> R.mipmap.slight_right
        Instruction.TURN_RIGHT -> R.mipmap.right
        Instruction.TURN_SHARP_RIGHT -> R.mipmap.sharp_right
        Instruction.FINISH -> R.drawable.marker_icon_red
        Instruction.REACHED_VIA -> R.drawable.marker_icon_red
        Instruction.USE_ROUNDABOUT -> R.mipmap.roundabout
        Instruction.KEEP_RIGHT -> R.mipmap.keep_right
        Instruction.U_TURN_RIGHT -> R.mipmap.u_turn_right

        else -> R.mipmap.ic_launcher
    }

    companion object {
        fun toReadableTime(timeInSeconds: Long?): String {
            if(timeInSeconds == null) return ""

            val timeInMinutes = timeInSeconds / 1000 / 60
            val optionalLessThan = if (timeInMinutes > 0) "" else "<"
            return "$optionalLessThan${timeInMinutes}min"
        }

        fun toReadableDistance(distance: Double?): String = if (distance == null) "" else String.format("%.0fm", distance, Locale.ROOT)
    }

}