package de.ironjan.arionav_fw.ionav.navigation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.ionav.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.mapview.IndoorItemTapCallback
import de.ironjan.arionav_fw.ionav.mapview.SimpleMapViewViewModel
import de.ironjan.arionav_fw.ionav.routing.RoutingService
import kotlinx.android.synthetic.main.fragment_simple_map_nav.*

class SimpleMapViewFragment : Fragment(R.layout.fragment_simple_map_nav) {


    private val viewModel: SimpleMapViewViewModel
        get() = mapView.viewModel

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        mapView.onLifecycleOwnerAttached(lifecycleOwner)

        val ionavContainer = (activity?.application as IonavContainerHolder).ionavContainer
        mapView.initialize(ionavContainer)


        observeViewModel(lifecycleOwner)


        loadSuggestions(lifecycleOwner)

        btnCenterOnUser.setOnClickListener {
            val coordinate =
                ionavContainer.positioningService
                    .lastKnownLocation
                    .value
                    ?.coordinate
                    ?: return@setOnClickListener

            mapView.centerOn(coordinate)
        }

        btnStartNavigation.setOnClickListener {
9            val namedPlaces = ionavContainer.namedPlaceRepository.getPlaces().value
                ?: return@setOnClickListener
            val destinationString = edit_destination.text.toString()
            val namedPlace = namedPlaces[destinationString]
            if (namedPlace == null) {
                Snackbar.make(btnCenterOnUser, "Could not find $destinationString.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ionavContainer.navigationService.destination = namedPlace.coordinate
        }

        btnLevelPlus.setOnClickListener { viewModel.increaseLevel()}
        btnLevelMinus.setOnClickListener { viewModel.decreaseLevel()}

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

    private fun observeViewModel(lifecycleOwner: LifecycleOwner) {
        viewModel.selectedLevel.observe(lifecycleOwner, Observer { txtLevel.text = it.toString() })
        viewModel.routingStatus.observe(lifecycleOwner, Observer { btnStartNavigation.isEnabled = (it == RoutingService.Status.READY) })

        viewModel.initializationStatus.observe(lifecycleOwner, Observer {
            val isLoading = it != SimpleMapViewViewModel.InitializationStatus.INITIALIZED

            progress.visibility = if(isLoading) View.VISIBLE else View.GONE

            progress.isIndeterminate = isLoading
        })
    }

    private fun loadSuggestions(lifecycleOwner: LifecycleOwner) {
        val context = context ?: return

        val endSuggestionsAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        edit_destination.setAdapter(endSuggestionsAdapter)

        when (val application = activity?.application) {
            is IonavContainerHolder -> {
                val placesLiveData = application.ionavContainer.namedPlaceRepository
                    .getPlaces()
                placesLiveData.observe(lifecycleOwner, Observer {
                    endSuggestionsAdapter.apply {
                        clear()
                        addAll(it.keys)
                    }
                })
            }
            else -> {
            }
        }

    }


}
