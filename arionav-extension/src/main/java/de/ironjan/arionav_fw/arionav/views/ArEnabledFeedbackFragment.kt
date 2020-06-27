package de.ironjan.arionav_fw.arionav.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.ironjan.arionav_fw.arionav.ArEnabledNavigationFragmentHost
import de.ironjan.arionav_fw.arionav.R
import de.ironjan.arionav_fw.arionav.model.feedback.ArFeedback
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.util.Mailer
import kotlinx.android.synthetic.main.ar_fragment_feedback.*

class ArEnabledFeedbackFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.ar_fragment_feedback, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSendFeedback.setOnClickListener { sendFeedback() }
        btnSkipFeedback.setOnClickListener { goToMapView() }
    }

    private fun sendFeedback() {
        val feedback = ArFeedback(
            ratingBarOverall.rating,
            ratingBarMapNav.rating,
            ratingBarArNav.rating,
            editAdditionalComments.text.toString()
        )

        val holder = activity?.application as IonavContainerHolder
        Mailer(holder.ionavContainer.developerMails)
            .sendPrefilledFeedback(context ?: return, feedback.toString())

        goToMapView()
    }

    private fun goToMapView() {
        (activity as? ArEnabledNavigationFragmentHost)?.goToMapView(true)
    }
}