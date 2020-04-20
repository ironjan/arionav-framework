package de.ironjan.arionav_fw.sample.util

import de.ironjan.arionav_fw.ionav.positioning.bluetooth.BluetoothPositioningProviderImplementation
import de.ironjan.arionav_fw.ionav.positioning.gps.GpsPositionProvider
import de.ironjan.arionav_fw.ionav.positioning.wifi.WifiPositioningProvider

object PreferenceKeys {
    val ENABLED_BLUETOOTH = enabledKey(BluetoothPositioningProviderImplementation.BLUETOOTH_PROVIDER_NAME)
    val ENABLED_WIFI = enabledKey(WifiPositioningProvider.WIFI_POSITIONING_PROVIDER)
    val ENABLED_GPS = enabledKey(GpsPositionProvider.GPS_PROVIDER_NAME)

    fun enabledKey(providerName: String) = "ENABLED_$providerName"

    fun priorityKey(providerName: String) = "PRIORITY_$providerName"

    val PRIORITY_BLUETOOTH = priorityKey(BluetoothPositioningProviderImplementation.BLUETOOTH_PROVIDER_NAME)
    val PRIORITY_WIFI =  priorityKey(WifiPositioningProvider.WIFI_POSITIONING_PROVIDER)
    val PRIORITY_GPS =  priorityKey(GpsPositionProvider.GPS_PROVIDER_NAME)


}