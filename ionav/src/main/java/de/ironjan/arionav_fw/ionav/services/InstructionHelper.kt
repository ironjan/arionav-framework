package de.ironjan.arionav_fw.ionav.services

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.ionav.R

class InstructionHelper(private val context: Context) {
    fun toText(currentInstruction: Instruction) = toText(currentInstruction, null)
    fun toText(currentInstruction: Instruction, nextInstruction: Instruction?): String {
        val instructionText = if(nextInstruction==null) "" else getTextFor(nextInstruction.sign)


        val distance = " %.2f".format(currentInstruction.distance)

        val time = currentInstruction.time
        val durationMinString = toReadableTime(time)
        return "$instructionText in ${distance}m ($durationMinString)\n${nextInstruction?.name}"
    }

    fun getTextFor(sign: Int): String {
        return when (sign) {
            -98 -> context.resources.getString(R.string.instruction_u_turn_unknown)
            -8 -> context.resources.getString(R.string.instruction_u_turn_left)
            -7 -> context.resources.getString(R.string.instruction_keep_left)
            -6 -> context.resources.getString(R.string.instruction_leave_roundabout)
            -3 -> context.resources.getString(R.string.instruction_turn_sharp_left)
            -2 -> context.resources.getString(R.string.instruction_turn_left)
            -1 -> context.resources.getString(R.string.instruction_turn_slight_left)
            0 -> context.resources.getString(R.string.instruction_continue_on_street)
            1 -> context.resources.getString(R.string.instruction_turn_slight_right)
            2 -> context.resources.getString(R.string.instruction_turn_right)
            3 -> context.resources.getString(R.string.instruction_turn_sharp_right)
            4 -> context.resources.getString(R.string.instruction_finish)
            5 -> context.resources.getString(R.string.instruction_reached_via)
            6 -> context.resources.getString(R.string.instruction_use_roundabout)
            Integer.MIN_VALUE -> "IGNORED"
            7 -> context.resources.getString(R.string.instruction_keep_right)
            8 -> context.resources.getString(R.string.instruction_u_turn_right)
            else -> context.resources.getString(R.string.instruction_unknown)
        }
    }

    fun getInstructionImageFor(sign: Int?): Drawable? {
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
            4 -> R.drawable.marker_icon_red
            5 -> R.drawable.marker_icon_red
            6 -> R.mipmap.roundabout
//            Integer.MIN_VALUE -> "IGNORE"
            7 -> R.mipmap.keep_right
            8 -> R.mipmap.u_turn_right

            // destination
            SIGN_DESTINATION -> R.drawable.marker_icon_red
            else -> R.mipmap.ic_launcher
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.resources.getDrawable(resId, context.theme)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getDrawable(resId)
        }
    }

    companion object {
        const val SIGN_DESTINATION = 1337



        fun toReadableTime(timeInSeconds: Long): String {
            val timeInMinutes = timeInSeconds / 1000 / 60
            val optionalLessThan = if (timeInMinutes > 0) "" else "<"
            val durationMinString = "$optionalLessThan${timeInMinutes}min"
            return durationMinString
        }
    }

}