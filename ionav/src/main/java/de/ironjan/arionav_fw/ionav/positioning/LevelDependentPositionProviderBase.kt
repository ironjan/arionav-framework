package de.ironjan.arionav_fw.ionav.positioning

import android.content.Context
import androidx.lifecycle.Lifecycle
import de.ironjan.arionav_fw.ionav.util.Observer

abstract class LevelDependentPositionProviderBase(
    context: Context,
    lifecycle: Lifecycle,
    positioningService: PositioningService
) : PositionProviderBaseImplementation(context, lifecycle) {

    init {
        positioningService.registerObserver(object : Observer<PositioningServiceState> {
            override fun update(t: PositioningServiceState) {
                currentLevel = t.userSelectedLevel
            }
        })
    }

    var currentLevel: Double = 0.0
        private set
}