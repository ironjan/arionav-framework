package de.ironjan.arionav_fw.ionav.positioning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.slf4j.LoggerFactory

class PositioningService : IPositioningServiceObservable {


    override var lastKnownPosition: IonavLocation? = null
        private set

    override var lastUpdate: Long = -1L
        private set

    var userSelectedLevel: Double = 0.0
        set(value) {
            field = value
            notifyObservers()
        }

   // region observer handling
   private val positionObservers: MutableList<IPositionObserver> = mutableListOf()
    override fun registerObserver(observer: IPositionObserver) {
        if (positionObservers.contains(observer)) return
        positionObservers.add(observer)
    }

    override fun removeObserver(observer: IPositionObserver) {
        positionObservers.remove(observer)
    }
    private val serviceObservers = mutableListOf<IPositioningServiceObserver>()
    override fun registerObserver(observer: IPositioningServiceObserver) {
        if(serviceObservers.contains(observer)) return
            serviceObservers.add(observer)
    }

    override fun removeObserver(observer: IPositioningServiceObserver) {
        serviceObservers.remove(observer)
    }
    override fun notifyObservers() {
        logger.debug("PositioningService notifying observers.")
        positionObservers.forEach { o ->
            val position = lastKnownPosition ?: return@forEach
            o.update(position)
        }
        serviceObservers.forEach { o ->
            o.updateUserSelectedLevel(userSelectedLevel)

            val position = lastKnownPosition ?: return@forEach
            o.update(position)
        }
    }
    // endregion

    private val logger = LoggerFactory.getLogger(PositioningService::class.java.simpleName)

    private val _providers: MutableList<IPositionProvider> = mutableListOf()
    private val _providerLiveData = MutableLiveData<List<IPositionProvider>>(_providers)
    val providers: LiveData<List<IPositionProvider>> = _providerLiveData


    private val _locationHistory = mutableListOf<IonavLocation>()
    val locationHistory: List<IonavLocation> = _locationHistory

    private val observer: IPositionObserver = object : IPositionObserver {
        override fun update(c: IonavLocation?) {
            updateLastLocation(c)
        }
    }

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
            val newEnough = System.currentTimeMillis() - it.lastUpdate < 30000
            val positionKnown = it.lastKnownPosition != null
            logger.info("Location update by ${it.name}: $isEnabled, $newEnough,  $positionKnown..")
            isEnabled && positionKnown && newEnough
        }?.lastKnownPosition



        logger.warn("c: $c, newLocation: $newLocation")
        lastKnownPosition = newLocation
        lastUpdate = newLocation?.timestamp ?: lastUpdate
        notifyObservers()
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

    private fun isRegistered(provider: IPositionProvider): Boolean = _providers.map { it.name }.contains(provider.name)

    fun unregisterProvider(provider: IPositionProvider) {
        logger.info("Unregistering $provider")
        _providers.remove(provider)
        _providerLiveData.value = _providers
        provider.removeObserver(observer)
        if (provider.enabled) {
            provider.stop()
        }
    }

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

    fun setPriority(prio: Int, provider: IPositionProvider) {
        _providers.remove(provider)
        _providers.add(prio, provider)
    }

    fun getProvider(name: String): IPositionProvider? {
        return _providers.filter { it.name == name }.firstOrNull()
    }

    fun enableProvider(providerName: String) {
        val provider = getProvider(providerName) ?: throw IllegalArgumentException("Provider with name $providerName not registered.")
        enableProvider(provider)
    }
    fun enableProvider(provider: IPositionProvider) {
        if(provider.enabled) return

        provider.start()
    }

    fun disableProvider(providerName: String) {
            val provider = getProvider(providerName) ?: throw IllegalArgumentException("Provider with name $providerName not registered.")
            disableProvider(provider)
    }
    fun disableProvider(provider: IPositionProvider) {
        if(provider.disabled) return

        provider.stop()
    }

    companion object {
        val TAG = "PositioningService"
    }


}
