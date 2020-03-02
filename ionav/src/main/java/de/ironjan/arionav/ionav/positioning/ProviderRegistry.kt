package de.ironjan.arionav.ionav.positioning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.slf4j.LoggerFactory

class ProviderRegistry private constructor() {
    private val logger = LoggerFactory.getLogger(ProviderRegistry::class.java.simpleName)

    private val _providers: MutableList<IPositionProvider> = mutableListOf()
    private val _providerLiveData = MutableLiveData<List<IPositionProvider>>(_providers)
    val providers: LiveData<List<IPositionProvider>> = _providerLiveData

    fun registerProvider(provider: IPositionProvider) {
        _providers.add(provider)
        _providerLiveData.value = _providers
    }

    fun unregisterProvider(provider: IPositionProvider) {
        _providers.remove(provider)
        _providerLiveData.value = _providers
    }

    fun swapPriorities(pos1: Int, pos2: Int) {
        val tmp = _providers[pos1]
        _providers[pos1] = _providers[pos2]
        _providers[pos2] = tmp

        _providerLiveData.value = _providers

        _providers.forEach {logger.warn(it.name)}
    }

    companion object {
        val Instance = ProviderRegistry()
    }
}