package de.ironjan.arionav_fw.ionav.views

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
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.views.mapview.IndoorItemTapCallback
import de.ironjan.arionav_fw.ionav.services.RoutingService
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import kotlinx.android.synthetic.main.fragment_simple_map_nav.*

open class MapViewFragment : Fragment() {
    protected val viewModel by activityViewModels<IonavViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_simple_map_nav, container, false)

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        observeViewModel(viewLifecycleOwner)
        bindOnClickListeners()


        val holder = activity?.application as IonavContainerHolder
        viewModel.initialize(holder.ionavContainer)
        mapView.onLifecycleOwnerAttached(viewLifecycleOwner)
        mapView.initialize(viewModel)
        bindMapItemTapListener()
    }

    private fun observeViewModel(lifecycleOwner: LifecycleOwner) {
        viewModel.selectedLevel.observe(lifecycleOwner, Observer { txtLevel.text = it.toString() })
        viewModel.routingStatus.observe(lifecycleOwner, Observer { btnStartNavigation.isEnabled = (it == RoutingService.Status.READY) })

        viewModel.initializationStatus.observe(lifecycleOwner, Observer {
            val isLoading = it != IonavViewModel.InitializationStatus.INITIALIZED

            progress.visibility = if (isLoading) View.VISIBLE else View.GONE

            progress.isIndeterminate = isLoading
        })

        viewModel.destinationString.observe(lifecycleOwner, Observer { edit_destination.setText(it) })

        bindSuggestions(lifecycleOwner)
    }

    private fun bindSuggestions(lifecycleOwner: LifecycleOwner) {
        val context = context ?: return

        val endSuggestionsAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        edit_destination.setAdapter(endSuggestionsAdapter)

        viewModel.indoorData.observe(lifecycleOwner, Observer {
            endSuggestionsAdapter.apply {
                clear()
                addAll(it.names)
                sort { o1: String, o2: String -> o1.compareTo(o2) }
            }
        })
    }

    private fun bindOnClickListeners() {
        btnCenterOnUser.setOnClickListener {
            viewModel.setFollowUserPosition(true)
        }

        btnStartNavigation.setOnClickListener { startNavigation() }

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

                startNavigation()
            }
        }
    }

    private fun startNavigation() {
        val activity = activity ?: return

        val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = activity.currentFocus
        inputManager.hideSoftInputFromWindow(if (null == currentFocus) null else currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val destinationString = edit_destination.text.toString()

        val isPlaceFound = viewModel.setDestinationString(destinationString)
        val isPlaceNotFound = !isPlaceFound
        if (isPlaceNotFound) {
            Snackbar.make(btnCenterOnUser, "Could not find $destinationString.", Snackbar.LENGTH_SHORT).show()
            return
        }
    }
}
