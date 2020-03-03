package de.ironjan.arionav.ionav.positioning

interface IPositionObserver {
  fun onPositionChange(c: IonavLocation?)
}