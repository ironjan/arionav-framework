package de.ironjan.arionav_fw.samples.campus.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import de.ironjan.arionav_fw.samples.campus.BuildConfig

object Mailer {
    internal fun sendFeedback(context: Context?) {
        if (context == null) {
            return
        }
        val subject = String.format("ARIONAV Sample Feedback %s", BuildConfig.VERSION_NAME)

        val osVersion = Build.VERSION.RELEASE
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        val body = """Phone: $manufacturer $model
Android version: $osVersion

Feedback:

"""
        sendMailToDevs(context, subject, body)
    }

    private val developerMails = arrayOf("irb-git+mtljan-thesis-6522-24oauqa8lahux188otoxb4l1i-issue@mail.uni-paderborn.de")

    /**
     * Sends an email with the given subject and body. Notifies the user that an email app should be
     * set up, if there is none available.
     *
     * @param context a non-null context
     * @param subject The mail's subject
     * @param body The mail's body
     */
    fun sendMailToDevs(context: Context, subject: String, body: String) =
        sendMail(context, subject, body, developerMails)

    /**
     * Sends an email with the given subject and body. Notifies the user that an email app should be
     * set up, if there is none available.
     *
     * @param to the receivers
     * @param subject The mail's subject
     * @param body The mail's body
     * @param context a non-null context
     */
    fun sendMail(context: Context, subject: String, body: String, to: Array<String> = arrayOf()) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, to)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, body)

        val activityExists = intent.resolveActivity(context.packageManager) != null
        if (activityExists) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No mail app installed.", Toast.LENGTH_LONG).show()
        }
    }

    /** Cuts logs into half until it is small enough for an intent */
    private fun trim(logs: String): String {
        var trimmed = logs
        while (trimmed.toByteArray().size > 256000) {
            trimmed = "..." + trimmed.substring(trimmed.length / 2)
        }
        return trimmed
    }
}