package de.ironjan.arionav_fw.ionav.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.ironjan.arionav_fw.ionav.positioning.IPositionObserver
import de.ironjan.arionav_fw.ionav.positioning.IPositionProvider
import de.ironjan.arionav_fw.ionav.positioning.IonavLocation
import de.ironjan.arionav_fw.ionav.util.Observable
import de.ironjan.arionav_fw.ionav.util.Observer
import org.slf4j.LoggerFactory

private val Long.lessThan30SecondsAgo: Boolean
    get() = System.currentTimeMillis() - this < 30000

class PositioningService : Observable<PositioningServiceState> {

    // region state
    override var state = PositioningServiceState()
        private set(value) {
            field = value
            notifyObservers()
        }

    val lastKnownPosition: IonavLocation?
        get() = state.lastKnownPosition

    var userSelectedLevel: Double
        get() = state.userSelectedLevel
        set(value) {
            state = state.copy(userSelectedLevel = value)
        }
    //endregion

    // region state related methods and location history

    private val _locationHistory = mutableListOf<IonavLocation>()
    val locationHistory: List<IonavLocation> = _locationHistory

    private fun updateLastLocation(c: IonavLocation?) {
        logger.warn("Received update: $c")

//        if(c == null) return
//        if(System.currentTimeMillis() - c.timestamp < 10000) {
//            logger.debug("Update too soon. Ignored.")
//            return
//        }

        val newLocation = _providers.firstOrNull {
            // FIXME use better algorithm
            val isEnabled = it.enabled
            val newEnough = it.lastUpdate.lessThan30SecondsAgo
            val positionKnown = it.lastKnownPosition != null
            logger.info("Location update by ${it.name}: $isEnabled, $newEnough,  $positionKnown..")
            isEnabled && positionKnown && newEnough
        }?.lastKnownPosition



        logger.warn("c: $c, newLocation: $newLocation")
        state = state.copy(
            lastKnownPosition = newLocation,
            lastUpdate = newLocation?.timestamp ?: state.lastUpdate
        )
        logger.warn("Updated location to $newLocation")

        prependToLocationHistory(newLocation)
    }

    private fun prependToLocationHistory(location: IonavLocation?) {
        location ?: return

        _locationHistory.add(0, location)
        while (_locationHistory.size > 100) {
            _locationHistory.removeAt(100)
        }
    }
    //endregion

    // region observer handling
    private val _observers = mutableListOf<Observer<PositioningServiceState>>()

    override fun registerObserver(observer: Observer<PositioningServiceState>) {
        if (_observers.contains(observer)) return
        _observers.add(observer)
    }

    override fun removeObserver(observer: Observer<PositioningServiceState>) {
        _observers.remove(observer)
    }

    override fun notifyObservers() {
        logger.debug("PositioningService notifying observers.")

        _observers.forEach { it.update(state) }
    }
    // endregion

    private val logger = LoggerFactory.getLogger(PositioningService::class.java.simpleName)

    // region provider handling
    private val _providers: MutableList<IPositionProvider> = mutableListOf()
    private val _providerLiveData = MutableLiveData<List<IPositionProvider>>(_providers)
    val providers: LiveData<List<IPositionProvider>> = _providerLiveData


    fun registerProvider(provider: IPositionProvider, start: Boolean = false) {
        logger.info("Registering $provider. AutoStart = $start")

        if (isRegistered(provider)) return

        _providers.add(provider)
        _providerLiveData.value = _providers
        provider.registerObserver(observer)
        if (start) {
            provider.start()
        }
    }

    fun unregisterProvider(provider: IPositionProvider) {
        logger.info("Unregistering $provider")
        _providers.remove(provider)
        _providerLiveData.value = _providers
        provider.removeObserver(observer)
        if (provider.enabled) {
            provider.stop()
        }
    }

    fun setPriority(prio: Int, provider: IPositionProvider) {
        _providers.remove(provider)
        _providers.add(prio, provider)
    }

    private fun isRegistered(provider: IPositionProvider): Boolean = _providers.map { it.name }.contains(provider.name)


    fun removeProvider(name: String) {
        _providers.filter { it.name == name }
            .forEach { unregisterProvider(it) }
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

    fun enableProvider(providerName: String) {
        val provider = getProvider(providerName) ?: throw IllegalArgumentException("Provider with name $providerName not registered.")
        enableProvider(provider)
    }

    fun enableProvider(provider: IPositionProvider) {
        if (provider.enabled) return

        provider.start()
    }

    fun disableProvider(providerName: String) {
        val provider = getProvider(providerName) ?: throw IllegalArgumentException("Provider with name $providerName not registered.")
        disableProvider(provider)
    }

    fun disableProvider(provider: IPositionProvider) {
        if (provider.disabled) return

        provider.stop()
    }

    // endregion

    private val observer: IPositionObserver = object : IPositionObserver {
        override fun update(c: IonavLocation?) {
            updateLastLocation(c)
        }
    }


}
