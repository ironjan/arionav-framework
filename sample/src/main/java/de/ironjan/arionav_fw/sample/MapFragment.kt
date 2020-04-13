package de.ironjan.arionav_fw.sample

import android.os.AsyncTask
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
import de.ironjan.arionav_fw.framework.arionav.viewmodel.ArExtensionViewModel
import de.ironjan.arionav_fw.ionav.GhzExtractor
import de.ironjan.arionav_fw.ionav.IonavContainerHolder
import de.ironjan.arionav_fw.ionav.mapview.IndoorLayer
import de.ironjan.arionav_fw.ionav.routing.model.NamedPlace
import de.ironjan.arionav_fw.ionav.routing.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.routing.model.readers.IndoorMapDataLoadingTask
import de.ironjan.arionav_fw.ionav.util.InstructionHelper
import de.ironjan.graphhopper.extensions_core.Coordinate
import kotlinx.android.synthetic.main.fragment_map.*
import org.oscim.layers.vector.geometries.PolygonDrawable
import org.oscim.layers.vector.geometries.RectangleDrawable
import org.oscim.layers.vector.geometries.Style
import org.slf4j.LoggerFactory


class MapFragment : Fragment() {
    private lateinit var instructionHelper: InstructionHelper

    private val logger = LoggerFactory.getLogger(MapFragment::class.java.simpleName)

    private val ghzResId = R.raw.uni_paderborn

    private val mapName = "uni_paderborn"
    private lateinit var ghzExtractor: GhzExtractor
    private val model: ArExtensionViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_map, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // todo move to VM or below
        val context = context ?: return
        ghzExtractor = GhzExtractor()

        instructionHelper = InstructionHelper(context)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.initialize((activity?.application as IonavContainerHolder).ionavContainer)


        model.setMapViewViewModel(mapView.viewModel)

        buttonCenterOnPos.setOnClickListener {
            val tmp = viewModel.getFollowUserPositionLiveData().value ?: false
            viewModel.setFollowUserPosition(true)
            viewModel.centerOnUserPos()
            viewModel.setFollowUserPosition(tmp)
        }

        buttonLocationAsStart.setOnClickListener {
            mapView.viewModel.setStartCoordinateToUserPos()
        }



        buttonMapFollowLocation.setOnClickListener { mapView.viewModel.toggleFollowUserPosition() }
        buttonRemainingRoute.setOnClickListener { mapView.viewModel.toggleShowRemainingRoute() }

        val selectedPoi = arguments?.getString("selectedPoiCoordinate")
        if (selectedPoi != null) {
            logger.info("Selected poi from bundle: $selectedPoi")
            val coordinate = Coordinate.fromString(selectedPoi)
            mapView.viewModel.setEndCoordinate(coordinate)
            mapView.centerOn(coordinate)

            logger.info("Centered map on $coordinate")
        }

        val lifecycleOwner = this as? LifecycleOwner ?: throw IllegalArgumentException("LifecycleOwner not found.")
        attachLifeCycleOwnerToMapView(lifecycleOwner)
        registerLiveDataObservers(lifecycleOwner)
        prepareRoomAndPoiHandling(lifecycleOwner)

        if (!mapView.viewModel.hasBothCoordinates) {
            mapView.viewModel.setStartCoordinate(Coordinate(51.718060, 8.748366, 0.0))
            mapView.viewModel.setEndCoordinate(Coordinate(51.718631, 8.749061, 0.0))

        }

        loadAndShowIndoorData()
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    private lateinit var placesLiveData: LiveData<Map<String, NamedPlace>>

    private val suggestionsList = mutableListOf<String>()

    private val placesList = mutableSetOf<String>()
    private lateinit var endSuggestionsAdapter: ArrayAdapter<String>
    private lateinit var startSuggestionsAdapter: ArrayAdapter<String>
    private fun prepareRoomAndPoiHandling(lifecycleOwner: LifecycleOwner) {
        val context = context ?: return
        startSuggestionsAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, suggestionsList)
        edit_start_coordinates.setAdapter(startSuggestionsAdapter)
        endSuggestionsAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, suggestionsList)
        edit_end_coordinates.setAdapter(endSuggestionsAdapter)

        when (val application = activity?.application) {
            is ArionavSampleApplication -> {
                placesLiveData = application.sampleAppContainer.namedPlaceRepository
                    .getPlaces(application.ionavContainer.osmFilePath)
                placesLiveData.observe(lifecycleOwner, Observer {
                    placesList.apply {
                        clear()
                        addAll(it.keys)
                    }
                    updateSuggestionsList(startSuggestionsAdapter)
                    updateSuggestionsList(endSuggestionsAdapter)
                })
            }
        }



        edit_end_coordinates.doOnTextChanged { text, _, _, _ ->
            setCoordinateFromTextInput(text, false)
        }
        edit_start_coordinates.doOnTextChanged { text, _, _, _ ->
            setCoordinateFromTextInput(text, true)
        }


    }

    private fun setCoordinateFromTextInput(text: CharSequence?, isStart: Boolean) {
        logger.info("Text is $text.")
        val textAsString = text.toString()

        val place = placesLiveData.value?.get(textAsString)

        val coordinate = when {
            place != null -> {
                logger.info("Got place: $place.")

                place.coordinate

            }
            else -> null
        }
        if (coordinate == null) {
            return
        }

        if (isStart) {
            viewModel.setStartCoordinate(coordinate)
        } else {
            viewModel.setEndCoordinate(coordinate)
        }

        val lContext = context ?: return
        Toast.makeText(lContext, "Found a place with name $text. Replacing end coordinate with $coordinate.", Toast.LENGTH_LONG).show()
    }

    private fun updateSuggestionsList(adapter: ArrayAdapter<String>) {
        adapter.apply {
            clear()
            addAll(placesList)
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
        viewModel.getRemainingRouteLiveData().observe(lifecycleOwner, Observer {
            if(viewModel.getShowRemainingRouteCurrentValue()) {
                val instructions = it?.instructions?.take(2) ?: return@Observer

                val currentInstruction = instructions.first()
                val nextInstruction = instructions.last()

                val text = instructionHelper.toText(currentInstruction, nextInstruction)

                txtCurrentInstruction.setText(text)
            }
        })
        viewModel.getLevelListLiveData().observe(lifecycleOwner, Observer {
            val lContext = context ?: return@Observer
            spinnerLevel.adapter = ArrayAdapter(lContext, android.R.layout.simple_spinner_dropdown_item, it)
        })
        viewModel.getSelectedLevelListPosition().observe(lifecycleOwner, Observer {
            spinnerLevel.setSelection(it)
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


    private fun loadAndShowIndoorData() {

        val osmFilePath = when (val ionavHolder = activity?.application) {
            is IonavContainerHolder -> ionavHolder.ionavContainer.osmFilePath
            else -> null
        } ?: return


        val callback = object : IndoorMapDataLoadingTask.OnIndoorMapDataLoaded {
            override fun loadCompleted(indoorData: IndoorData) {
                showIndoorMapData(indoorData)
            }

        }

        IndoorMapDataLoadingTask(osmFilePath, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


    }

    private fun showIndoorMapData(indoorData: IndoorData) {
        logger.info("Completed loading of indoor map data: ${indoorData.indoorWays.count()} ways and ${indoorData.indoorNodes.count()} nodes.")
        val map = mapView.map()
        val selectedLevel = viewModel.getSelectedLevel()
        val indoorLayer = IndoorLayer(map, indoorData, selectedLevel)
        map.layers().add(indoorLayer)

        indoorData.getNodes(selectedLevel).map {
            val geometry = RectangleDrawable(it.toGeoPoint(), it.toGeoPoint())
            indoorLayer.add(geometry)
        }

        val roomStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(-0x660033ff)
            .fillColor(-0x660033ff)
            .strokeWidth(1 * resources.displayMetrics.density)
            .build()
        val otherStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(-0x66ff33ff)
            .fillColor(-0x22ff33ff)
            .strokeWidth(1 * resources.displayMetrics.density)
            .build()
        indoorData.getWays(selectedLevel).map { iw ->
            try {
                val map1 = iw.nodeRefs.map { it.toGeoPoint() }
                val geometry = PolygonDrawable(map1)
                geometry.style = when (iw.type) {
                    "room" -> roomStyle
                    else -> otherStyle
                }
                indoorLayer.add(geometry)
            } catch (e: Exception) {
                val y = e
            }
        }

    }


}