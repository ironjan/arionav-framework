package de.ironjan.arionav_fw.ionav.positioning.wifi.model

import android.net.wifi.ScanResult

data class SignalStrengthResult(
    val scanResult: ScanResult,
    val level: Int
) {
    val BSSID
        get() = scanResult.BSSID

    companion object {
        val maxLevel = 100
    }
}