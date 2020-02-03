package de.ironjan.arionav.ionav.positioning

import de.ironjan.graphhopper.extensions_core.Coordinate

interface IPositionObserver {
  fun onPositionChange(c: Coordinate)
}