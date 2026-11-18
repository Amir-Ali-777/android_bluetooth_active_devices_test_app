package com.example.bluetoothtestapp.bluetooth.active

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager

/**
 * Android 11 (API 30). There is no list made for calls and no LE Audio, so both kinds of entry
 * come from the general list of audio outputs: Bluetooth SCO for two-way audio, A2DP for media.
 */
class LegacyActiveDeviceResolver(context: Context) : ActiveDeviceResolver(context) {

    override fun bluetoothCommunicationOutputs(): List<AudioDeviceInfo> =
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .filter { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }

    override fun bluetoothMediaOutputs(): List<AudioDeviceInfo> =
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .filter { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
}
