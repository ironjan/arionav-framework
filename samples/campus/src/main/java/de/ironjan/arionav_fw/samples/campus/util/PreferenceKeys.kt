package de.ironjan.arionav_fw.samples.campus.util

import de.ironjan.arionav_fw.ionav.positioning.bluetooth.BluetoothPositionProvider
import de.ironjan.arionav_fw.ionav.positioning.gps.GpsPositionPositionProvider
import de.ironjan.arionav_fw.ionav.positioning.wifi.WifiPositionProvider

object PreferenceKeys {
    val ENABLED_BLUETOOTH = enabledKey(BluetoothPositionProvider.BLUETOOTH_PROVIDER_NAME)
    val ENABLED_WIFI = enabledKey(WifiPositionProvider.WIFI_POSITIONING_PROVIDER)
    val ENABLED_GPS = enabledKey(GpsPositionPositionProvider.GPS_PROVIDER_NAME)

    fun enabledKey(providerName: String) = "ENABLED_$providerName"

    fun priorityKey(providerName: String) = "PRIORITY_$providerName"

    val PRIORITY_BLUETOOTH = priorityKey(BluetoothPositionProvider.BLUETOOTH_PROVIDER_NAME)
    val PRIORITY_WIFI =  priorityKey(WifiPositionProvider.WIFI_POSITIONING_PROVIDER)
    val PRIORITY_GPS =  priorityKey(GpsPositionPositionProvider.GPS_PROVIDER_NAME)


}