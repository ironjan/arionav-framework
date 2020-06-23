package de.ironjan.arionav_fw.ionav.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.model.feedback.Feedback
import de.ironjan.arionav_fw.ionav.util.Mailer
import kotlinx.android.synthetic.main.fragment_feedback.*

class FeedbackFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_feedback, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSendFeedback.setOnClickListener { sendFeedback() }
    }

    private fun sendFeedback() {
        val feedback = Feedback(
            ratingBarOverall.rating,
            ratingBarMapNav.rating,
            ratingBarArNav.rating,
            editAdditionalComments.text.toString()
        )

        val holder = activity?.application as IonavContainerHolder
        Mailer(holder.ionavContainer.developerMails)
            .sendPrefilledFeedback(context ?: return, feedback.toString())
    }
}