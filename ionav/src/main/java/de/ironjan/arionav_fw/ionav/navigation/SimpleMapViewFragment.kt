package de.ironjan.arionav_fw.ionav.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.ionav.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.mapview.IndoorItemTapCallback
import de.ironjan.arionav_fw.ionav.mapview.SimpleMapViewViewModel
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import kotlinx.android.synthetic.main.fragment_simple_map_nav.*
import org.slf4j.LoggerFactory


open class SimpleMapViewFragment : Fragment() {
    private val logger = LoggerFactory.getLogger(SimpleMapViewFragment::class.simpleName)

    private val viewModel: SimpleMapViewViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_simple_map_nav, container, false)

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        mapView.onLifecycleOwnerAttached(lifecycleOwner)

        val ionavContainer = (activity?.application as IonavContainerHolder).ionavContainer
        mapView.initialize(ionavContainer)

        val navigationService = ionavContainer.navigationService

        observeViewModel(lifecycleOwner)
        bindSuggestions(lifecycleOwner)

        bindOnClickListeners(navigationService)

        bindMapItemTapListener()
    }

    private fun bindSuggestions(lifecycleOwner: LifecycleOwner) {
        val context = context ?: return

        val endSuggestionsAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        edit_destination.setAdapter(endSuggestionsAdapter)

        viewModel.indoorData.observe(lifecycleOwner, Observer {
            endSuggestionsAdapter.apply {
                clear()
                addAll(it.names)
            }
        })
    }

    private fun observeViewModel(lifecycleOwner: LifecycleOwner) {
        viewModel.selectedLevel.observe(lifecycleOwner, Observer { txtLevel.text = it.toString() })
        viewModel.routingStatus.observe(lifecycleOwner, Observer { btnStartNavigation.isEnabled = (it == RoutingService.Status.READY) })

        viewModel.initializationStatus.observe(lifecycleOwner, Observer {
            val isLoading = it != SimpleMapViewViewModel.InitializationStatus.INITIALIZED

            progress.visibility = if (isLoading) View.VISIBLE else View.GONE

            progress.isIndeterminate = isLoading
        })
    }

    private fun bindOnClickListeners(navigationService: NavigationService) {
        btnCenterOnUser.setOnClickListener {
            mapView.centerOnUser()
        }

        btnStartNavigation.setOnClickListener {
            val activity = activity ?: return@setOnClickListener

            val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val currentFocus = activity.currentFocus
            inputManager.hideSoftInputFromWindow(if (null == currentFocus) null else currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val namedPlaces = viewModel.indoorData.value
                ?: return@setOnClickListener
            val destinationString = edit_destination.text.toString()
            val namedPlace = namedPlaces.getWayByName(destinationString)
            if (namedPlace == null) {
                Snackbar.make(btnCenterOnUser, "Could not find $destinationString.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            navigationService.destination = namedPlace.center
        }

        btnLevelPlus.setOnClickListener { viewModel.increaseLevel() }
        btnLevelMinus.setOnClickListener { viewModel.decreaseLevel() }
    }

    private fun bindMapItemTapListener() {
        mapView.itemTapCallback = object : IndoorItemTapCallback {
            override fun singleTap(placeName: String) {
                edit_destination.setText(placeName)
            }

            override fun longTap(placeName: String) {
                edit_destination.setText(placeName)
                btnStartNavigation.callOnClick()
            }
        }
    }


}
