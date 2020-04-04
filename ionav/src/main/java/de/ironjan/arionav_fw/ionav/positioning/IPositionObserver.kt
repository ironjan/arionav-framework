package de.ironjan.arionav_fw.ionav.positioning

interface IPositionObserver {
  fun onPositionChange(c: IonavLocation?)
}