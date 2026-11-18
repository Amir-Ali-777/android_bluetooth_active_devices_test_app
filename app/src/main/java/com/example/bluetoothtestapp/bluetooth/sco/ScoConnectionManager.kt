package com.example.bluetoothtestapp.bluetooth.sco

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log

/**
 * Opens and closes the SCO link: the two-way, call-quality Bluetooth audio channel that carries
 * the headset microphone. Android always opens SCO to the headset it has made active for calls.
 * For an LE Audio headset (Android 13+) the same request opens LE Audio's two-way voice stream
 * instead of SCO; this class calls both "SCO" to keep the app's wording simple.
 *
 * The flow is the same on every Android version:
 * 1. switch the audio mode to [AudioManager.MODE_IN_COMMUNICATION], so Android treats the app
 *    like a voice call,
 * 2. ask Android for SCO ([requestSco]),
 * 3. wait until Android reports that the link is up ([notifyConnected]).
 *
 * Only steps 2 and 3 use different APIs on Android 11 and 12+, so those are what
 * [LegacyScoConnectionManager] and [ModernScoConnectionManager] implement. Every step is logged
 * with the tag `ScoConnectionManager`, so you can follow it in Logcat.
 *
 * All calls and callbacks happen on the main thread.
 */
abstract class ScoConnectionManager(context: Context) {

    protected val context: Context = context.applicationContext
    protected val audioManager = this.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var onConnected: (() -> Unit)? = null
    private var onDisconnected: (() -> Unit)? = null
    private var isConnected = false

    /**
     * Asks Android to open SCO to the active call headset.
     *
     * @param onConnected called once the SCO link is up and the headset microphone can be recorded.
     * @param onDisconnected called if the link drops after it was up, for example when the headset
     *   is switched off.
     * @return false if Android rejected the request straight away.
     */
    fun start(onConnected: () -> Unit, onDisconnected: () -> Unit): Boolean {
        this.onConnected = onConnected
        this.onDisconnected = onDisconnected
        isConnected = false
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        val accepted = requestSco()
        Log.d(TAG, "${javaClass.simpleName}: SCO requested, accepted=$accepted")
        return accepted
    }

    /**
     * Closes SCO and puts the audio mode back to normal. Android's stop API is called even when
     * SCO isn't running, so you can see in Logcat how the platform reacts to that.
     */
    fun stop() {
        Log.d(TAG, "${javaClass.simpleName}: stopping SCO, connected=$isConnected")
        onConnected = null
        onDisconnected = null
        isConnected = false
        releaseSco()
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    /** Asks Android to open SCO with the API of this Android version. Returns false if rejected. */
    protected abstract fun requestSco(): Boolean

    /** Asks Android to close SCO and stops listening for SCO changes. Safe to call at any time. */
    protected abstract fun releaseSco()

    /** Writes [message] to Logcat with the tag `ScoConnectionManager`. */
    protected fun log(message: String) {
        Log.d(TAG, message)
    }

    /** Called by subclasses when Android reports that the SCO link is up. */
    protected fun notifyConnected() {
        if (isConnected) return
        isConnected = true
        Log.d(TAG, "SCO connected")
        onConnected?.invoke()
    }

    /** Called by subclasses when Android reports that the SCO link is down. Ignored if it was never up. */
    protected fun notifyDisconnected() {
        if (!isConnected) return
        isConnected = false
        Log.d(TAG, "SCO disconnected")
        onDisconnected?.invoke()
    }

    companion object {
        private const val TAG = "ScoConnectionManager"

        /** Picks the SCO manager for the Android version the app is running on. */
        fun create(context: Context): ScoConnectionManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ModernScoConnectionManager(context)
            } else {
                LegacyScoConnectionManager(context)
            }
    }
}
