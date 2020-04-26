package de.ironjan.arionav_fw.ionav.positioning

import android.content.Context
import androidx.lifecycle.Lifecycle

abstract class LevelDependentPositionProviderBase(
    context: Context,
    lifecycle: Lifecycle,
    positioningService: PositioningService
) : PositionProviderBaseImplementation(context, lifecycle) {

    init {
        positioningService.registerObserver(object: IPositioningServiceObserver{
            override fun updateUserSelectedLevel(level: Double) {
                currentLevel = level
            }

            override fun update(t: IonavLocation?) { /* ignored */ }
        })
    }

    var currentLevel: Double = 0.0
        private set
}