package com.example.bluetoothtestapp.bluetooth.sco

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.bluetoothtestapp.bluetooth.describe

/**
 * SCO on Android 12+ (API 31+).
 *
 * - Open: pick the Bluetooth headset from [AudioManager.getAvailableCommunicationDevices] and
 *   select it with [AudioManager.setCommunicationDevice]. The app doesn't choose the transport:
 *   for a classic headset (TYPE_BLUETOOTH_SCO) Android opens SCO, for an LE Audio headset
 *   (TYPE_BLE_HEADSET, Android 13+) it opens LE Audio's two-way voice stream.
 * - Link state: [AudioManager.OnCommunicationDeviceChangedListener]. Android reports the headset
 *   as the communication device only once the link is actually up.
 * - Close: [AudioManager.clearCommunicationDevice].
 */
@RequiresApi(Build.VERSION_CODES.S)
class ModernScoConnectionManager(context: Context) : ScoConnectionManager(context) {

    private var isListenerRegistered = false

    private val communicationDeviceListener = AudioManager.OnCommunicationDeviceChangedListener { device ->
        log("Communication device is now ${device?.describe() ?: "none"}")
        if (device?.isBluetoothHeadset() == true) notifyConnected() else notifyDisconnected()
    }

    override fun requestSco(): Boolean {
        val headset = audioManager.availableCommunicationDevices.firstOrNull { it.isBluetoothHeadset() }
        if (headset == null) {
            log("No Bluetooth headset in availableCommunicationDevices")
            return false
        }
        // On classic Bluetooth the entry stands for "the active headset"; Android picks which one.
        log("setCommunicationDevice(${headset.describe()})")
        audioManager.addOnCommunicationDeviceChangedListener(
            ContextCompat.getMainExecutor(context),
            communicationDeviceListener
        )
        isListenerRegistered = true
        val accepted = audioManager.setCommunicationDevice(headset)
        // If the link was already up (for example opened by another app), no change will be reported.
        if (accepted && audioManager.communicationDevice?.isBluetoothHeadset() == true) notifyConnected()
        return accepted
    }

    override fun releaseSco() {
        audioManager.clearCommunicationDevice()
        if (isListenerRegistered) {
            audioManager.removeOnCommunicationDeviceChangedListener(communicationDeviceListener)
            isListenerRegistered = false
        }
    }

    private fun AudioDeviceInfo.isBluetoothHeadset() =
        type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || type == AudioDeviceInfo.TYPE_BLE_HEADSET
}
