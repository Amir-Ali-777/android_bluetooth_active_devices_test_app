package com.example.bluetoothtestapp.bluetooth.connected

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context

/**
 * Android 11 (API 30). Bluetooth access comes from the BLUETOOTH permission, which is granted
 * at install time, so there is nothing to check at runtime.
 */
class LegacyConnectedDevicesDetector(context: Context) : ConnectedDevicesDetector(context) {

    override val watchedProfiles = mapOf(
        BluetoothProfile.HEADSET to BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED,
        BluetoothProfile.A2DP to BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED
    )

    override fun hasPermission() = true
}
