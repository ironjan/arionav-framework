package de.ironjan.arionav.ionav.positioning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.slf4j.LoggerFactory

class PositioningProviderRegistry private constructor() {
    private val logger = LoggerFactory.getLogger(PositioningProviderRegistry::class.java.simpleName)

    private val _providers: MutableList<IPositionProvider> = mutableListOf()
    private val _providerLiveData = MutableLiveData<List<IPositionProvider>>(_providers)
    val providers: LiveData<List<IPositionProvider>> = _providerLiveData


    private val _lastKnownLocation: MutableLiveData<IonavLocation?> = MutableLiveData(null)
    val lastKnownLocation: LiveData<IonavLocation?> = _lastKnownLocation

    private val _locationHistory = mutableListOf<IonavLocation>()
    private val _locationHistoryLiveData = MutableLiveData(_locationHistory.toList())
    val locationHistory: LiveData<List<IonavLocation>> = _locationHistoryLiveData

    private val observer: IPositionObserver = object : IPositionObserver {
        override fun onPositionChange(c: IonavLocation?) {
            updateLastLocation(c)
        }
    }

    private fun updateLastLocation(c: IonavLocation?) {
        val newLocation = _providers.lastOrNull {
            // FIXME use better algorithm
            val isEnabled = it.enabled
            val newEnough = System.currentTimeMillis() - it.lastUpdate < 30000
            val positionKnown = it.lastKnownPosition != null
            logger.debug("Position from ${it.name} is... known = $positionKnown; newEnough = $newEnough ")
           isEnabled &&  positionKnown && newEnough
        }?.lastKnownPosition

        _lastKnownLocation.value = newLocation

        appendToLocationHistory(newLocation)
    }

    private fun appendToLocationHistory(location: IonavLocation?){
        val location = location ?: return

        _locationHistory.apply {
            val newHistory = _locationHistory.take(100) + location
            clear()
            addAll(newHistory)
        }
        _locationHistoryLiveData.value = _locationHistory

    }


    fun registerProvider(provider: IPositionProvider, start: Boolean = false) {
        _providers.add(provider)
        _providerLiveData.value = _providers
        provider.registerObserver(observer)
        if(start){provider.start()}
    }

    fun unregisterProvider(provider: IPositionProvider) {
        _providers.remove(provider)
        _providerLiveData.value = _providers
        provider.removeObserver(observer)
        if(provider.enabled) {provider.stop()}
    }

    fun swapPriorities(pos1: Int, pos2: Int) {
        val tmp = _providers[pos1]
        _providers[pos1] = _providers[pos2]
        _providers[pos2] = tmp

        _providerLiveData.value = _providers

        _providers.forEach { logger.warn(it.name) }
    }

    fun getProvider(name: String): IPositionProvider? {
        return _providers.filter { it.name == name }.firstOrNull()
    }

    companion object {
        val Instance = PositioningProviderRegistry()
    }
}