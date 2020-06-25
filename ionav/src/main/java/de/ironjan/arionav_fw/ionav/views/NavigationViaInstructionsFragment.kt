package de.ironjan.arionav_fw.ionav.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.arionav_fw.ionav.views.debug.TextInstructionAdapter
import kotlinx.android.synthetic.main.fragment_navigation_via_text.*

open class NavigationViaInstructionsFragment : Fragment() {

    open val viewModel: IonavViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_navigation_via_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        val lifecycleOwner = viewLifecycleOwner


        viewModel.remainingDistanceToDestination.observe(viewLifecycleOwner, Observer {
            updateDestinationSnackbar(it)
        })

        val adapter = TextInstructionAdapter(lifecycleOwner, viewModel)
        recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

    }


    // region destination snackbar
    var destinationSnackbar: Snackbar? = null

    private fun updateDestinationSnackbar(distanceToDestination: Double?) {
        if(distanceToDestination == null || distanceToDestination > 5.0) {
            clearDestinationSnackbar()
        } else {
            showDestinationSnackbar()
        }
    }

    private fun showDestinationSnackbar() {
        destinationSnackbar = (
                destinationSnackbar ?:
                Snackbar.make(findViewById<View>(R.id.navigationModeBottomBar)!!, "You are close to your destination.", Snackbar.LENGTH_INDEFINITE)
                    .apply {
                        setAction("OK") {
                            dismiss()
                            goToFeedback()
                        }
                    })
            .apply {
                if(!isShownOrQueued){
                    show()
                }
            }
    }

    protected fun goToFeedback() {
        (activity as? NavigationFragmentHost)?.goToFeedback()
    }
    private fun clearDestinationSnackbar() {
        destinationSnackbar?.dismiss()
        destinationSnackbar = null
    }
    // endregion
}