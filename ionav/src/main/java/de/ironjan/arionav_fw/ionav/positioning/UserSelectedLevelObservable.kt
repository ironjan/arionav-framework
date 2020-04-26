package de.ironjan.arionav_fw.ionav.positioning

interface IPositioningServiceObservable: IPositionObservable {
    fun registerObserver(observer: IPositioningServiceObserver)

    fun removeObserver(observer: IPositioningServiceObserver)

}
interface IPositioningServiceObserver: IPositionObserver {
    fun updateUserSelectedLevel(level: Double)
}