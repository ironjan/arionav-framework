package de.ironjan.arionav_fw.arionav.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.graphhopper.util.Instruction
import de.ironjan.arionav_fw.arionav.R
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.arionav_fw.ionav.views.NavigationFragmentHost
import de.ironjan.arionav_fw.ionav.views.findViewById
import kotlinx.android.synthetic.main.fragment_ar_view.*

open class NavigationViaArFragment : Fragment() {
    // region view model
    open val viewModel: IonavViewModel by activityViewModels()
    // endregion

    protected val ar_route_view: ArRouteView
        get() = this.findViewById<ArRouteView>(R.id.ar_route_view)!!

    // region lifecycle events
    protected lateinit var instructionHelper: InstructionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instructionHelper = InstructionHelper(context ?: return)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_ar_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ar_route_view.observe(viewModel, viewLifecycleOwner)

        viewModel.remainingDistanceToDestination.observe(viewLifecycleOwner, Observer {
            updateDestinationSnackbar(it)
        })

        setupActionBar()


        bindNavigationModeBottomBarWithAr()
    }

    // endregion

    // region action bar

    private fun setupActionBar() {
        (requireActivity() as AppCompatActivity)
            .supportActionBar
            ?.apply {
                val view = layoutInflater.inflate(de.ironjan.arionav_fw.ionav.R.layout.action_bar_start_navigation, null)

                bindActionBarViewsToViewModel(view)

                setCustomView(view, ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT))

                displayOptions =
                    (ActionBar.DISPLAY_SHOW_HOME
                            or ActionBar.DISPLAY_HOME_AS_UP
                            or ActionBar.DISPLAY_SHOW_TITLE
                            or ActionBar.DISPLAY_SHOW_CUSTOM)

                setHasOptionsMenu(true)
            }
    }

    private fun bindActionBarViewsToViewModel(view: View) {
        viewModel.destinationString.observe(viewLifecycleOwner,
            Observer {
                view.findViewById<TextView>(de.ironjan.arionav_fw.ionav.R.id.txtDestination).text = it
            })

        viewModel.remainingDistanceToDestination.observe(viewLifecycleOwner,
            Observer {
                view.findViewById<TextView>(de.ironjan.arionav_fw.ionav.R.id.txtDistance).text = InstructionHelper.toReadableDistance(it)
            })
        viewModel.remainingDurationToDestination.observe(viewLifecycleOwner,
            Observer {
                view.findViewById<TextView>(de.ironjan.arionav_fw.ionav.R.id.txtDuration).text = InstructionHelper.toReadableTime(it)
            })
    }
    // endregion

    // region ar instruction view

    protected open fun updateInstructionView(view: View, currentInstruction: Instruction, nextInstruction: Instruction) {
        val txtName = view.findViewById<TextView>(R.id.instructionText)
        val txtDistance = view.findViewById<TextView>(R.id.instructionDistanceInMeters)
        val instructionImage = view.findViewById<ImageView>(R.id.instructionImage)

        txtName.text = instructionHelper.getTextFor(nextInstruction.sign)
        txtDistance.text = InstructionHelper.toReadableDistance(currentInstruction.distance)
        instructionImage.setImageDrawable(instructionHelper.getInstructionImageFor(nextInstruction.sign))
    }
    // endregion

    // region destination snackbar
    var destinationSnackbar: Snackbar? = null

    private fun updateDestinationSnackbar(distanceToDestination: Double?) {
        if (distanceToDestination == null || distanceToDestination > 5.0) {
            clearDestinationSnackbar()
        } else {
            showDestinationSnackbar()
        }
    }

    private fun showDestinationSnackbar() {
        destinationSnackbar = (
                destinationSnackbar ?: Snackbar.make(ar_route_view, "You are close to your destination.", Snackbar.LENGTH_INDEFINITE)
                    .apply {
                        setAction("OK") {
                            dismiss()
                            goToFeedback()
                        }
                    })
            .apply {
                if (!isShownOrQueued) {
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
