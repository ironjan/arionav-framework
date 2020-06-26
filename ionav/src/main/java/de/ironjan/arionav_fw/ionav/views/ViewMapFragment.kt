package de.ironjan.arionav_fw.ionav.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ProgressBar
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
import de.ironjan.arionav_fw.ionav.services.RoutingService
import de.ironjan.arionav_fw.ionav.viewmodel.IonavViewModel
import de.ironjan.arionav_fw.ionav.views.mapview.IndoorItemTapCallback
import de.ironjan.graphhopper.extensions_core.Coordinate
import kotlinx.android.synthetic.main.fragment_view_map.*
import org.oscim.core.GeoPoint


open class ViewMapFragment : Fragment() {
    private var closeToDestinationSnackbar: Snackbar? = null

    protected open val viewModel: IonavViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.fragment_view_map, container, false)

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setHasOptionsMenu(true)
            setDisplayShowCustomEnabled(false)
            displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_HOME_AS_UP
        }



        val holder = activity?.application as IonavContainerHolder
        viewModel.initialize(holder.ionavContainer)
        mapView.onLifecycleOwnerAttached(viewLifecycleOwner)
        mapView.initialize(viewModel, object : IonavMapView.LongPressCallback {
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
        bindOnClickListeners()
        observeViewModel(viewLifecycleOwner)
        bindMapItemTapListener()
    }

    private fun observeViewModel(lifecycleOwner: LifecycleOwner) {
        viewModel.selectedLevel.observe(lifecycleOwner, Observer { txtLevel.text = it.toString() })

        viewModel.initializationStatus.observe(lifecycleOwner, Observer {
            val isLoading = it != IonavViewModel.InitializationStatus.INITIALIZED

            (activity as? AppCompatActivity)
                ?.supportActionBar
                ?.apply {
                    view
                        ?.findViewById<ProgressBar>(android.R.id.progress)
                        ?.apply {
                            visibility = if (isLoading) View.VISIBLE else View.GONE
                            isIndeterminate = isLoading
                        }
                }
        })

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

//        btnBackToSearch.setOnClickListener { viewModel.setDestination(null) }
    }

    private fun bindMapItemTapListener() {
        mapView.itemTapCallback = object : IndoorItemTapCallback {
            override fun singleTap(name: String) {
                setDestinationString(name)
            }

            override fun longTap(name: String) {
                setDestinationStringAndStartNavigate(name)
            }
        }
    }

    protected fun setDestinationStringAndStartNavigate(name: String) {
        val coordinate = viewModel.getCoordinateOf(name) ?: return
        viewModel.setDestinationAndName(name, coordinate)
        goToStartNavigationFragment()
    }

    protected fun setDestinationString(name: String) {
        viewModel.setDestinationString(name)
    }

    private fun goToStartNavigationFragment() {
        val navigationFragmentHost = activity as? NavigationFragmentHost
        navigationFragmentHost?.goToStartNavigation()
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

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                startNavigationFromName(query)

                hideKeyboard()


                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
        val endSuggestionsAdapter = ArrayAdapter(context ?: return, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())

        val searchAutoComplete =
            searchView
                .findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
                .apply {
                    setAdapter(endSuggestionsAdapter)
                    setOnItemClickListener { _, _, position, _ ->
                        startNavigationFromName(endSuggestionsAdapter.getItem(position))
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

    private fun hideKeyboard() {
        try {
            val activity = activity ?: return
            val inputManager =
                (activity.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            inputManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startNavigationFromName(item1: String?) {
        val item = item1 ?: return
        val coordinate = viewModel.getCoordinateOf(item) ?: return
        viewModel.setDestinationAndName(item, coordinate)

        goToStartNavigationFragment()
    }
    // endregion
}
