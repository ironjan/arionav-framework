package de.ironjan.arionav_fw.samples.tourism.services

import de.ironjan.arionav_fw.ionav.model.osm.Node
import de.ironjan.arionav_fw.ionav.services.DestinationServiceState
import de.ironjan.graphhopper.extensions_core.Coordinate

class TourismDestinationServiceState(
    destinations: Map<String, Coordinate>,
    val destinationNodes: Map<String, Node>
) : DestinationServiceState(destinations)