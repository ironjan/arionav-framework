package de.ironjan.arionav.ionav.positioning

interface PositionProvider {
    fun addListener(positionListenerBaseImplementation: PositionListenerBaseImplementation)
}