package de.ironjan.arionav.ionav.positioning

interface PositionProvider {
    fun addListener(positionProviderBaseImplementation: PositionProviderBaseImplementation)
}