package de.ironjan.arionav_fw.ionav.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.arionav_fw.ionav.views.mapview.IndoorItemTapCallback
import kotlinx.android.synthetic.main.fragment_simple_map_nav.*
import org.oscim.core.GeoPoint


open class StartNavigationFragment : Fragment() {
    private var closeToDestinationSnackbar: Snackbar? = null

    protected open val viewModel: IonavViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_start_navigation, container, false)

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()

        observeViewModel(viewLifecycleOwner)
        bindOnClickListeners()


        val holder = activity?.application as IonavContainerHolder
        viewModel.initialize(holder.ionavContainer)
        mapView.onLifecycleOwnerAttached(viewLifecycleOwner)
        mapView.initialize(viewModel, object : IonavMapView.LongPressCallback {
            override fun longPress(p: GeoPoint): Boolean {
                /* nothing to do */
                return true
            }
        })
        bindMapItemTapListener()
    }

    private fun setupActionBar() {
        (requireActivity() as AppCompatActivity)
            .supportActionBar
            ?.apply {
                val view = layoutInflater.inflate(R.layout.view_start_navigation_bar, null)

                bindActionBarViewsToViewModel(view)

                setCustomView(view, ActionBar.LayoutParams(MATCH_PARENT, MATCH_PARENT))

                displayOptions =
                    (ActionBar.DISPLAY_SHOW_HOME
                            or ActionBar.DISPLAY_HOME_AS_UP
                            or ActionBar.DISPLAY_SHOW_TITLE
                            or ActionBar.DISPLAY_SHOW_CUSTOM)

                setHasOptionsMenu(false)
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
            if(it==null || it > 5.0) {
                clearBeingCloseToDestinationNotification()
            }else {
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
                    viewModel.setDestination(null)
                }
                show()
            }
    }

    private fun bindOnClickListeners() {
        btnCenterOnUser.setOnClickListener {
            viewModel.setFollowUserPosition(true)
        }


        btnLevelPlus.setOnClickListener { viewModel.increaseLevel() }
        btnLevelMinus.setOnClickListener { viewModel.decreaseLevel() }

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
