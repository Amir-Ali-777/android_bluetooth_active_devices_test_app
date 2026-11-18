package com.example.bluetoothtestapp.bluetooth.active

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.example.bluetoothtestapp.bluetooth.describe

/**
 * Finds the two devices Android routes audio to:
 * - the communication device: the Bluetooth device that gets two-way audio (calls, SCO, voice
 *   recording through its microphone). That is a classic headset (HFP) or, on Android 13+, an
 *   LE Audio headset;
 * - the media device: the device music and other media play through. Android Settings marks it
 *   as "Active".
 *
 * Both are reported together as [ActiveDevices]. They are often the same device, but not always.
 * For example, turning off "Media audio" for a headset in its Bluetooth settings keeps it as the
 * communication device, while media moves to the host.
 *
 * Two sources are combined for each:
 * - AudioManager's audio devices. Android registers only the active Bluetooth devices there, and
 *   [AudioDeviceCallback] reports every change. This works on Android 11 and 12+.
 * - The Bluetooth stack's broadcasts for classic Bluetooth, which name the device: "active device
 *   changed", and "SCO audio connected" / "media playing", because SCO and media only ever go to
 *   the active device. Some devices need them: their SCO entry has a placeholder
 *   address, and their media entry can keep the previous device's address after switching
 *   between two connected devices.
 *
 * Calls and media can be active on different headsets. For example, a headset that starts voice
 * recognition (a button press that opens the voice assistant) becomes the call headset, while
 * media stays where it was.
 *
 * Everything is logged with the tag `ActiveDeviceResolver`, including which headset SCO audio
 * actually connects to ([BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED]), whichever app opened it.
 *
 * Subclasses only decide where the Bluetooth communication and media entries are read from,
 * because that is what differs between Android versions.
 */
abstract class ActiveDeviceResolver(context: Context) {

    private val appContext = context.applicationContext
    protected val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var listener: ((ActiveDevices) -> Unit)? = null

    /** The classic headset the Bluetooth stack last named as active for calls since [start], if any. */
    private var announcedHeadset: String? = null

    /** The device the Bluetooth stack last named as active for media since [start], if any. */
    private var announcedMediaDevice: String? = null

    /** The Bluetooth two-way entries (SCO, LE Audio headset) AudioManager lists right now. */
    protected abstract fun bluetoothCommunicationOutputs(): List<AudioDeviceInfo>

    /** The Bluetooth media entries AudioManager lists right now. */
    protected abstract fun bluetoothMediaOutputs(): List<AudioDeviceInfo>

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) = publishActiveDevices()
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) = publishActiveDevices()
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device = IntentCompat
                .getParcelableExtra(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            when (intent.action) {
                ACTION_ACTIVE_HEADSET_CHANGED -> {
                    Log.d(TAG, "Bluetooth stack: active headset for calls is now ${device.describe()}")
                    announcedHeadset = device?.address
                    publishActiveDevices()
                }
                ACTION_ACTIVE_MEDIA_DEVICE_CHANGED -> {
                    Log.d(TAG, "Bluetooth stack: active media device is now ${device.describe()}")
                    announcedMediaDevice = device?.address
                    publishActiveDevices()
                }
                // SCO only opens to the active call headset, whichever app asked, so this names it
                // even when its announcement was missed, e.g. because the app started later.
                BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
                    Log.d(TAG, "Bluetooth stack: SCO audio ${scoAudioStateName(state)} on ${device.describe()}")
                    if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                        announcedHeadset = device?.address
                        publishActiveDevices()
                    }
                }
                // Likewise, media only streams to the active media device.
                BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED -> {
                    val isPlaying = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1) == BluetoothA2dp.STATE_PLAYING
                    Log.d(TAG, "Bluetooth stack: media ${if (isPlaying) "playing" else "stopped"} on ${device.describe()}")
                    if (isPlaying) {
                        announcedMediaDevice = device?.address
                        publishActiveDevices()
                    }
                }
            }
        }
    }

    /** Starts watching. [onActiveDevicesChanged] receives the active devices now and after every change. */
    fun start(onActiveDevicesChanged: (ActiveDevices) -> Unit) {
        if (listener != null) return
        listener = onActiveDevicesChanged
        audioManager.registerAudioDeviceCallback(deviceCallback, null)
        val filter = IntentFilter().apply {
            addAction(ACTION_ACTIVE_HEADSET_CHANGED)
            addAction(ACTION_ACTIVE_MEDIA_DEVICE_CHANGED)
            addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)
        }
        // Exported, because the broadcasts come from the Bluetooth system process.
        ContextCompat.registerReceiver(appContext, bluetoothReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
        publishActiveDevices()
    }

    fun stop() {
        if (listener == null) return
        audioManager.unregisterAudioDeviceCallback(deviceCallback)
        appContext.unregisterReceiver(bluetoothReceiver)
        announcedHeadset = null
        announcedMediaDevice = null
        listener = null
    }

    private fun publishActiveDevices() {
        val callback = listener ?: return
        val communicationOutputs = bluetoothCommunicationOutputs()
        val mediaOutputs = bluetoothMediaOutputs()
        val activeDevices = ActiveDevices(
            hasBluetoothCommunicationDevice = communicationOutputs.isNotEmpty(),
            communicationAddress = findCommunicationDevice(communicationOutputs),
            mediaAddress = findMediaDevice(mediaOutputs)
        )
        Log.d(
            TAG,
            "Two-way entries: ${communicationOutputs.map { it.describe() }}, announced headset: $announcedHeadset | " +
                "media entries: ${mediaOutputs.map { it.describe() }}, announced media device: $announcedMediaDevice | " +
                "result: $activeDevices"
        )
        callback(activeDevices)
    }

    /**
     * Android lists a Bluetooth two-way entry only while a headset is active for calls, so no
     * entry means none is, whatever the broadcasts said before. Most devices name the headset on
     * that entry; LE Audio headsets always do. Devices that only list a placeholder SCO entry
     * rely on the announced headset. Null when nobody knows which headset it is.
     */
    private fun findCommunicationDevice(communicationOutputs: List<AudioDeviceInfo>): String? = when {
        communicationOutputs.isEmpty() -> null
        else -> communicationOutputs.firstOrNull { it.hasRealAddress }?.address ?: announcedHeadset
    }

    /** No Bluetooth media entry means media plays on the host; otherwise the announced device wins. */
    private fun findMediaDevice(mediaOutputs: List<AudioDeviceInfo>): String? {
        val realOutputs = mediaOutputs.filter { it.hasRealAddress }
        return when {
            realOutputs.isEmpty() -> null
            announcedMediaDevice != null -> announcedMediaDevice
            else -> realOutputs.first().address
        }
    }

    private fun scoAudioStateName(state: Int): String = when (state) {
        BluetoothHeadset.STATE_AUDIO_CONNECTING -> "connecting"
        BluetoothHeadset.STATE_AUDIO_CONNECTED -> "connected"
        BluetoothHeadset.STATE_AUDIO_DISCONNECTED -> "disconnected"
        else -> "state $state"
    }

    /** Skips placeholder entries that some devices list with an empty or all-zero address. */
    private val AudioDeviceInfo.hasRealAddress: Boolean
        get() = address.isNotEmpty() && address != "00:00:00:00:00:00"

    companion object {
        private const val TAG = "ActiveDeviceResolver"

        // Not in the public SDK, so there are no constants to import. Android 9+ sends them for
        // classic Bluetooth only, and Settings uses them for its "Active" labels.
        private const val ACTION_ACTIVE_HEADSET_CHANGED =
            "android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED"
        private const val ACTION_ACTIVE_MEDIA_DEVICE_CHANGED =
            "android.bluetooth.a2dp.profile.action.ACTIVE_DEVICE_CHANGED"

        /** Picks the resolver for the Android version the app is running on. */
        fun create(context: Context): ActiveDeviceResolver =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ModernActiveDeviceResolver(context)
            } else {
                LegacyActiveDeviceResolver(context)
            }
    }
}
