package de.ironjan.arionav.ionav.positioning

import de.ironjan.graphhopper.extensions_core.Coordinate

@Deprecated("Use IPositionObserverV2 instead.")
interface IPositionObserver {
  fun onPositionChange(c: IonavLocation?)
}
interface IPositionObserverV2 : IPositionObserver {
  fun onPositionChange(c: IonavLocation?, provider: IPositionProvider)
}