package de.ironjan.arionav_fw.ionav.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import de.ironjan.arionav_fw.ionav.R
import de.ironjan.arionav_fw.ionav.di.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.services.InstructionHelper
import de.ironjan.arionav_fw.ionav.services.RoutingService
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.arionav_fw.ionav.views.mapview.IndoorItemTapCallback
import de.ironjan.graphhopper.extensions_core.Coordinate
import kotlinx.android.synthetic.main.fragment_simple_map_nav.*
import kotlinx.android.synthetic.main.view_search_bar.*
import kotlinx.android.synthetic.main.view_start_navigation_bar.*
import org.oscim.core.GeoPoint
import java.util.*


open class MapViewFragment : Fragment() {
    private var closeToDestinationSnackbar: Snackbar? = null

    protected open val viewModel: IonavViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_simple_map_nav, container, false)

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_SHOW_TITLE
            setHasOptionsMenu(true)
        }

        val findViewById = view.findViewById<View>(R.id.search_bar)
        findViewById.visibility = View.VISIBLE

        observeViewModel(viewLifecycleOwner)
        bindOnClickListeners()


        val holder = activity?.application as IonavContainerHolder
        viewModel.initialize(holder.ionavContainer)
        mapView.onLifecycleOwnerAttached(viewLifecycleOwner)
        mapView.initialize(viewModel, object: IonavMapView.LongPressCallback {
            override fun longPress(p: GeoPoint): Boolean {
                if (viewModel.routingStatus.value == RoutingService.Status.READY) {
                    val selectedLevel = viewModel.getSelectedLevel()

                    viewModel.setDestination(Coordinate(p.latitude, p.longitude, selectedLevel))
                    goToStartNavigationFragment()
                    return true
                }

                val msg = "Graph not loaded yet. Please wait."
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                return true
            }
        })
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

        viewModel.destinationString.observe(lifecycleOwner, Observer {
            edit_destination.setText(it)
            txtDestination.text = it
        })
        viewModel.remainingDistanceToDestination.observe(lifecycleOwner, Observer {
            if (it == null) {
                txtDistance.text = ""
                return@Observer
            }

            txtDistance.text = String.format("%.0fm", it, Locale.ROOT)
            if (it < 5.0) {
                notifyUserAboutBeingCloseToDestination()
            } else {
                clearBeingCloseToDestinationNotification()
            }
        })
        viewModel.remainingDurationToDestination.observe(lifecycleOwner, Observer { txtDuration.text = InstructionHelper.toReadableTime(it ?: return@Observer) })


        viewModel.route.observe(lifecycleOwner, Observer {
            when (it) {
                null -> {
                    view?.findViewById<View>(R.id.start_navigation_bar)?.visibility = View.GONE
                    view?.findViewById<View>(R.id.search_bar)?.visibility = View.VISIBLE
                }
                else -> {
                    view?.findViewById<View>(R.id.start_navigation_bar)?.visibility = View.VISIBLE
                    view?.findViewById<View>(R.id.search_bar)?.visibility = View.GONE
                }
            }


        })

        bindSuggestions(lifecycleOwner)
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

    private fun bindSuggestions(lifecycleOwner: LifecycleOwner) {
        val context = context ?: return

        val endSuggestionsAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        edit_destination.setAdapter(endSuggestionsAdapter)

        viewModel.destinations.observe(lifecycleOwner, Observer {
            endSuggestionsAdapter.apply {
                clear()
                addAll(it.keys)
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

//        btnBackToSearch.setOnClickListener { viewModel.setDestination(null) }
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

        when (val destination = viewModel.setDestinationString(destinationString)) {
            null -> Snackbar.make(btnCenterOnUser, "Could not find $destinationString.", Snackbar.LENGTH_SHORT).show()
            else -> {
                viewModel.setDestinationAndName(destinationString, destination)

            }
        }
    }
    private fun goToStartNavigationFragment() {
        (activity as? NavigationFragmentHost)?.goToStartNavigation()
    }

    // region options menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search, menu)

        setupSearchView(menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setupSearchView(menu: Menu): Unit {
        val searchView = menu.findItem(R.id.mnu_search).actionView as SearchView
        searchView.setIconifiedByDefault(false)

        val endSuggestionsAdapter = ArrayAdapter(context ?: return, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())

        val searchAutoComplete =
            searchView
                .findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
                .apply {
                    setAdapter(endSuggestionsAdapter)
                    setOnItemClickListener { parent, view, position, id ->
                        val item = endSuggestionsAdapter.getItem(position) ?: return@setOnItemClickListener
                        val coordinate = viewModel.getCoordinateOf(item) ?: return@setOnItemClickListener
                        viewModel.setDestinationAndName(item, coordinate)

                        goToStartNavigationFragment()
                    }
                }

        viewModel.destinations.observe(viewLifecycleOwner, Observer {
            endSuggestionsAdapter.apply {
                clear()
                addAll(it.keys)
                sort { o1: String, o2: String -> o1.compareTo(o2) }
            }
        })
        viewModel.destinationString.observe(viewLifecycleOwner, Observer(searchAutoComplete::setText))
    }
    // endregion
}
