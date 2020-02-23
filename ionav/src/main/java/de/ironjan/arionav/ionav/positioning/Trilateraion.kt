package de.ironjan.arionav.ionav.positioning

import android.net.wifi.WifiManager.calculateSignalLevel
import de.ironjan.graphhopper.extensions_core.Coordinate
import kotlin.math.round

object Trilateraion {
    fun naiveTrilateration(devices: List<SignalStrength>): Coordinate {
        // Coordinate -> weight
        // todo probably better to not use calculateSignal from wifi manager
        val coordToRssi = devices.map { it.coordinate to calculateSignalLevel(it.rssi, 10) / 10.0 }

        val latSum = coordToRssi.map { it.first.lat * it.second }.sum()
        val lonSum = coordToRssi.map { it.first.lon * it.second }.sum()
        val lvlSum = coordToRssi.map { it.first.lvl * it.second }.sum()
        val n = coordToRssi.map { it.second }.count().toDouble()

        val medLat = latSum / n
        val medLon = lonSum / n
        val medLvl = lvlSum / n

        val normalizedLevel = round(medLvl)

        return Coordinate(medLat, medLon, normalizedLevel)
    }
}