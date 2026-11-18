package com.example.bluetoothtestapp.bluetooth.sco

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

/**
 * SCO on Android 11 (API 30).
 *
 * - Open: [AudioManager.startBluetoothSco]. It always uses the active call headset; Android 11
 *   has no way to choose another one.
 * - Link state: the [AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED] broadcast, which reports
 *   connecting, connected and disconnected.
 * - Close: [AudioManager.stopBluetoothSco].
 *
 * These APIs are deprecated from Android 12, but they are the only way to do this on Android 11.
 */
@Suppress("DEPRECATION")
class LegacyScoConnectionManager(context: Context) : ScoConnectionManager(context) {

    private var isReceiverRegistered = false

    private val scoStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // The broadcast is sticky: the last known state arrives on registration; only real changes matter.
            if (isInitialStickyBroadcast) return
            val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR)
            log("SCO audio state is now $state (0 disconnected, 1 connected, 2 connecting, -1 error)")
            when (state) {
                AudioManager.SCO_AUDIO_STATE_CONNECTED -> notifyConnected()
                AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> notifyDisconnected()
            }
        }
    }

    override fun requestSco(): Boolean {
        if (!audioManager.isBluetoothScoAvailableOffCall) {
            log("SCO is not available off call")
            return false
        }
        context.registerReceiver(scoStateReceiver, IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED))
        isReceiverRegistered = true
        log("startBluetoothSco(): Android opens SCO to its active headset")
        audioManager.startBluetoothSco()
        return true
    }

    override fun releaseSco() {
        audioManager.stopBluetoothSco()
        if (isReceiverRegistered) {
            context.unregisterReceiver(scoStateReceiver)
            isReceiverRegistered = false
        }
    }
}
