package de.ironjan.arionav_fw.ionav.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.arionav_fw.ionav.views.mapview.IndoorItemTapCallback
import de.ironjan.graphhopper.extensions_core.Coordinate
import kotlinx.android.synthetic.main.fragment_simple_map_nav.btnCenterOnUser
import kotlinx.android.synthetic.main.fragment_simple_map_nav.btnLevelMinus
import kotlinx.android.synthetic.main.fragment_simple_map_nav.btnLevelPlus
import kotlinx.android.synthetic.main.fragment_simple_map_nav.mapView
import kotlinx.android.synthetic.main.fragment_simple_map_nav.txtLevel
import kotlinx.android.synthetic.main.fragment_start_navigation.*
import org.oscim.core.GeoPoint


open class StartNavigationFragment : Fragment() {
    private var closeToDestinationSnackbar: Snackbar? = null

    protected open val viewModel: IonavViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_start_navigation, container, false)

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onBackPressedDispatcher = requireActivity().onBackPressedDispatcher
        onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.setDestination(null)
            remove()
            onBackPressedDispatcher.onBackPressed()
        }

        setupActionBar()

        observeViewModel(viewLifecycleOwner)
        bindOnClickListeners()


        val holder = activity?.application as IonavContainerHolder
        viewModel.initialize(holder.ionavContainer)
        mapView.onLifecycleOwnerAttached(viewLifecycleOwner)
        mapView.initialize(viewModel, object : IonavMapView.LongPressCallback {
            override fun longPress(p: GeoPoint): Boolean {
                val selectedLevel = viewModel.getSelectedLevel()

                viewModel.setDestination(Coordinate(p.latitude, p.longitude, selectedLevel))

                return true
            }
        })
        bindMapItemTapListener()
    }

    private fun setupActionBar() {
        (requireActivity() as AppCompatActivity)
            .supportActionBar
            ?.apply {
                val view = layoutInflater.inflate(R.layout.action_bar_start_navigation, null)

                bindActionBarViewsToViewModel(view)

                setCustomView(view, ActionBar.LayoutParams(MATCH_PARENT, MATCH_PARENT))

                displayOptions =
                    (ActionBar.DISPLAY_SHOW_HOME
                            or ActionBar.DISPLAY_HOME_AS_UP
                            or ActionBar.DISPLAY_SHOW_TITLE
                            or ActionBar.DISPLAY_SHOW_CUSTOM)

                setHasOptionsMenu(true)
            }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                viewModel.setDestination(null)
                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun bindActionBarViewsToViewModel(view: View) {
        viewModel.destinationString.observe(viewLifecycleOwner,
            Observer {
                view.findViewById<TextView>(R.id.txtDestination).text = it
            })

        viewModel.remainingDistanceToDestination.observe(viewLifecycleOwner,
            Observer {
                view.findViewById<TextView>(R.id.txtDistance).text = InstructionHelper.toReadableDistance(it)
            })
        viewModel.remainingDurationToDestination.observe(viewLifecycleOwner,
            Observer {
                view.findViewById<TextView>(R.id.txtDuration).text = InstructionHelper.toReadableTime(it)
            })
    }

    private fun observeViewModel(lifecycleOwner: LifecycleOwner) {
        viewModel.selectedLevel.observe(lifecycleOwner, Observer { txtLevel.text = it.toString() })

        viewModel.remainingDistanceToDestination.observe(lifecycleOwner, Observer {
            if (it == null || it > 5.0) {
                clearBeingCloseToDestinationNotification()
            } else {
                notifyUserAboutBeingCloseToDestination()
            }
        })

    }

    open fun clearBeingCloseToDestinationNotification() {
        closeToDestinationSnackbar?.dismiss()
        closeToDestinationSnackbar = null
    }

    @SuppressLint("WrongConstant")
    open fun notifyUserAboutBeingCloseToDestination() {
        closeToDestinationSnackbar = closeToDestinationSnackbar ?: Snackbar.make(btnCenterOnUser, "You are close to your destination.", Snackbar.LENGTH_INDEFINITE)
            .apply {
                setAction("OK") {
                    dismiss()
                    navigationFragmentHost?.goToFeedback()
                    viewModel.setDestination(null)
                }
                show()
            }
    }

    protected open val navigationFragmentHost
        get() = activity as? NavigationFragmentHost

    private fun bindOnClickListeners() {
        btnCenterOnUser.setOnClickListener {
            viewModel.setFollowUserPosition(true)
        }


        btnLevelPlus.setOnClickListener { viewModel.increaseLevel() }
        btnLevelMinus.setOnClickListener { viewModel.decreaseLevel() }


        btnMapNav.setOnClickListener { navigationFragmentHost?.goToMapNavigation() }
// FIXME MOVE TO ARIONAVEXT        btnArNav.setOnClickListener { (activity as? NavigationFragmentHost) }
        btnTextInstructions.setOnClickListener { navigationFragmentHost?.goToInstrucitons() }
    }

    private fun bindMapItemTapListener() {
        mapView.itemTapCallback = object : IndoorItemTapCallback {
            override fun singleTap(placeName: String) {
                viewModel.setDestinationString(placeName)
            }

            override fun longTap(placeName: String) {
                val coordinate = viewModel.getCoordinateOf(placeName) ?: return
                viewModel.setDestinationAndName(placeName, coordinate)
            }
        }
    }

}
