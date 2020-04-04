package de.ironjan.arionav_fw.ionav.positioning

interface PositionProvider {
    fun addListener(positionProviderBaseImplementation: PositionProviderBaseImplementation)
}