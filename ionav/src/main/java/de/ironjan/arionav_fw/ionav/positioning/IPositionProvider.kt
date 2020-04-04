package de.ironjan.arionav_fw.ionav.positioning

interface IPositionProvider {
    val lastKnownPosition : IonavLocation?
    val lastUpdate : Long

    val name: String

    val enabled: Boolean

    fun registerObserver(observer: IPositionObserver)
    fun removeObserver(observer: IPositionObserver)
    fun notifyObservers()


    fun start()

    fun stop()

}

