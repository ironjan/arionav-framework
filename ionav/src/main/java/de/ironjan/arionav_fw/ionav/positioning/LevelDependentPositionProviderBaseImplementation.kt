package de.ironjan.arionav_fw.ionav.positioning

import android.content.Context
import androidx.lifecycle.Lifecycle

abstract class LevelDependentPositionProviderBaseImplementation(
    private val context: Context,
    private val lifecycle: Lifecycle
) : PositionProviderBaseImplementation(context, lifecycle) {
    private var _currentLevel: Double = 0.0

    var currentLevel: Double
        get() = _currentLevel
        set(value) {
            _currentLevel = value
        }
}