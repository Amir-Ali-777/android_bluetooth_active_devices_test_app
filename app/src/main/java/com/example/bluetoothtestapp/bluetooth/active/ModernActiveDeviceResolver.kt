package com.example.bluetoothtestapp.bluetooth.active

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Android 12+ (API 31+). Two-way entries come from the list made for calls,
 * [AudioManager.getAvailableCommunicationDevices]:
 * - TYPE_BLUETOOTH_SCO: the active classic headset (HFP);
 * - TYPE_BLE_HEADSET: the active LE Audio headset with a microphone (Android 13+ only, because
 *   Android 12 has no LE Audio support).
 *
 * TYPE_BLE_SPEAKER and hearing aids are left out: they only play sound, the microphone stays on
 * the host, so they are not two-way devices. Media entries come from the audio outputs, where
 * media can also play over LE Audio.
 */
@RequiresApi(Build.VERSION_CODES.S)
class ModernActiveDeviceResolver(context: Context) : ActiveDeviceResolver(context) {

    override fun bluetoothCommunicationOutputs(): List<AudioDeviceInfo> =
        audioManager.availableCommunicationDevices.filter {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || it.type == AudioDeviceInfo.TYPE_BLE_HEADSET
        }

    override fun bluetoothMediaOutputs(): List<AudioDeviceInfo> =
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).filter {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER
        }
}
