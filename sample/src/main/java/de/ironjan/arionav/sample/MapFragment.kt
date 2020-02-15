package de.ironjan.arionav.sample

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.ironjan.arionav.framework.PathWrapperJsonConverter
import de.ironjan.arionav.ionav.GhzExtractor
import de.ironjan.arionav.ionav.positioning.gps.GpsPositionProvider
import de.ironjan.arionav.ionav.special_routing.model.Poi
import de.ironjan.arionav.ionav.special_routing.model.Room
import de.ironjan.arionav.ionav.special_routing.repository.PoiRepository
import de.ironjan.arionav.ionav.special_routing.repository.RoomRepository
import de.ironjan.arionav.sample.viewmodel.SharedViewModel
import kotlinx.android.synthetic.main.fragment_map.*
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import kotlin.math.round


class MapFragment : Fragment() {
    private val logger = LoggerFactory.getLogger(MapFragment::class.java.simpleName)
    private lateinit var gpsPositionProvider: GpsPositionProvider

    private val ghzResId = ArionavSampleApplication.ghzResId

    private val mapName = ArionavSampleApplication.mapName
    private lateinit var ghzExtractor: GhzExtractor
    private val model: SharedViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // todo move to VM or below
        val lContext = context ?: return
        ghzExtractor = GhzExtractor(lContext.applicationContext, ghzResId, mapName)


        gpsPositionProvider = GpsPositionProvider(lContext, lifecycle)
        gpsPositionProvider.start()


}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.initialize(ghzExtractor)

        buttonCenterOnPos.setOnClickListener {
            mapView.centerOnUserPosition()
        }

        buttonLocationAsStart.setOnClickListener {
            val coordinate = gpsPositionProvider.lastKnownPosition ?: return@setOnClickListener
            mapView.viewModel.setStartCoordinate(coordinate)
        }


        mapView.viewModel.setUserPositionProvider(gpsPositionProvider)
        buttonMapFollowLocation.setOnClickListener { mapView.viewModel.toggleFollowUserPosition() }
        buttonRemainingRoute.setOnClickListener { mapView.viewModel.toggleShowRemainingRoute() }

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        attachLifeCycleOwnerToMapView(lifecycleOwner)
        registerLiveDataObservers(lifecycleOwner)
        prepareRoomHandling(lifecycleOwner)
    }

    override fun onResume() {
        super.onResume()
        model.inc()

    }

    private lateinit var roomLiveData: LiveData<Map<String, Room>>

    private lateinit var poiLiveData: LiveData<Map<String, Poi>>
    private val suggestionsList = mutableListOf<String>()
    private val roomList = mutableSetOf<String>()
    private val poiList = mutableSetOf<String>()
    private lateinit var endSuggestionsAdapter: ArrayAdapter<String>
    private lateinit var startSuggestionsAdapter: ArrayAdapter<String>

    private fun prepareRoomHandling(lifecycleOwner: LifecycleOwner) {
        val lContext = context ?: return
        startSuggestionsAdapter = ArrayAdapter(lContext, android.R.layout.simple_dropdown_item_1line, suggestionsList)
        edit_start_coordinates.setAdapter(startSuggestionsAdapter)
        endSuggestionsAdapter = ArrayAdapter(lContext, android.R.layout.simple_dropdown_item_1line, suggestionsList)
        edit_end_coordinates.setAdapter(endSuggestionsAdapter)

        roomLiveData = RoomRepository().getRooms(ghzExtractor.osmFilePath)
        poiLiveData = PoiRepository().getPois(ghzExtractor.osmFilePath)

        roomLiveData.observe(lifecycleOwner, Observer {
            replaceListContentWith(roomList, it.keys)
        })
        poiLiveData.observe(lifecycleOwner, Observer {
            replaceListContentWith(poiList, it.keys)
        })

        edit_end_coordinates.doOnTextChanged { text, _, _, _ ->
            logger.info("Text is $text.")
            val textAsString = text.toString()

            val room = roomLiveData.value?.get(textAsString)
            val poi = poiLiveData.value?.get(textAsString)

            val targetCoordinate = when {
                room != null -> {
                    logger.info("Got room: $room.")

                    room.coordinate

                }
                poi != null -> {
                    logger.info("Got POI: $poi")

                    poi.coordinate

                }
                else -> null
            }
            if (targetCoordinate != null) {
                viewModel.setEndCoordinate(targetCoordinate)

                val lContext = context ?: return@doOnTextChanged
                Toast.makeText(lContext, "Found a place with name $text. Replacing end coordinate with $targetCoordinate.", Toast.LENGTH_LONG).show()
            }
        }



    }

    private fun replaceListContentWith(list: MutableSet<String>, newContents: Set<String>) {
        list.apply {
            clear()
            addAll(newContents)
        }
        updateSuggestionsList(startSuggestionsAdapter)
        updateSuggestionsList(endSuggestionsAdapter)
    }

    private fun updateSuggestionsList(adapter: ArrayAdapter<String>) {
        adapter.apply {
            clear()
            addAll(poiList)
            addAll(roomList)
        }
    }

    private val viewModel
        get() = mapView.viewModel

    private fun registerLiveDataObservers(lifecycleOwner: LifecycleOwner) {
        viewModel.getStartCoordinateLifeData().observe(lifecycleOwner, Observer {
            edit_start_coordinates.setText(it?.asString() ?: "")
        })
        viewModel.getEndCoordinateLifeData().observe(lifecycleOwner, Observer {
            edit_end_coordinates.setText(it?.asString() ?: "")
        })
        viewModel.getFollowUserPositionLiveData().observe(lifecycleOwner, Observer {
            buttonMapFollowLocation.isChecked = it
        })
        viewModel.getShowRemainingRouteLiveData().observe(lifecycleOwner, Observer {
            buttonRemainingRoute.isChecked = it
        })
        viewModel.getLevelListLiveData().observe(lifecycleOwner, Observer {
            val lContext = context ?: return@Observer
            spinnerLevel.adapter = ArrayAdapter(lContext, android.R.layout.simple_spinner_dropdown_item, it)
        })
        viewModel.getSelectedLevelListPosition().observe(lifecycleOwner, Observer {
            spinnerLevel.setSelection(it)
        })
        viewModel.getNextInstructionLiveData().observe(lifecycleOwner, Observer {
            if (it == null) return@Observer
            if (viewModel.getShowRemainingRouteCurrentValue()) {

                val distance = round(it.distance * 100) / 100
                val instructionText = MainActivity.InstructionSignToText.getTextFor(it.sign)
                val timeInSeconds = it.time / 1000
                val timeInMinutes = timeInSeconds / 60
                val msg = "$instructionText ${it.name}, ${distance}m, ${timeInMinutes}min"

                txtCurrentInstruction.setText(msg)
            }
        })

        spinnerLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
            }

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, value: Long) {
                viewModel.selectLevelListPosition(pos)
                logger.info("Level List spinner was used. Selected position $pos...")
            }
        }

        viewModel.getCurrentRouteLiveData().observe(lifecycleOwner, Observer {
            val hasRoute = it != null
            button_AR.isEnabled = hasRoute
        })
        button_AR.setOnClickListener(this::onArButtonClick)

        model.getC().observe(lifecycleOwner, Observer { logger.warn("Counter Value increased to $it.") })
    }

    private fun attachLifeCycleOwnerToMapView(lifecycleOwner: LifecycleOwner) {
        mapView.onLifecycleOwnerAttached(lifecycleOwner)
    }

    private fun onArButtonClick(it: View) {
        val lDisplayedRoute = viewModel.getRemainingRouteLiveData().value
        if (lDisplayedRoute == null) {
            logger.info("AR button was clicked with null route. Ignoring.")
            // TODO show warning in ui
            return
        }

        logger.info("Switching to AR view.")
        findNavController().navigate(R.id.action_to_arViewFragment)
    }


}