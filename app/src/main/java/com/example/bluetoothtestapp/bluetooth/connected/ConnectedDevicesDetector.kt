package com.example.bluetoothtestapp.bluetooth.connected

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.example.bluetoothtestapp.model.AudioDevice
import com.example.bluetoothtestapp.model.AudioDeviceType
import com.example.bluetoothtestapp.model.ProfileState

/**
 * Detects connected Bluetooth audio devices and reports the full list every time it changes.
 *
 * Android tracks connections per profile (HEADSET for calls, A2DP for media, LE_AUDIO for both). For each
 * profile in [watchedProfiles] this class opens a profile proxy, which can list the devices
 * connected right now, and listens to that profile's connection broadcast, which fires on every
 * connect and disconnect. Subclasses only decide which profiles to watch and how the
 * permission is checked, because that is what differs between Android versions.
 */
abstract class ConnectedDevicesDetector(context: Context) {

    protected val context: Context = context.applicationContext

    /** Profile id to the broadcast action that announces connection changes for that profile. */
    protected abstract val watchedProfiles: Map<Int, String>

    protected abstract fun hasPermission(): Boolean

    private val adapter: BluetoothAdapter? =
        this.context.getSystemService(BluetoothManager::class.java)?.adapter
    private val proxies = mutableMapOf<Int, BluetoothProfile>()
    private var listener: ((List<AudioDevice>) -> Unit)? = null

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (listener == null) {
                adapter?.closeProfileProxy(profile, proxy)
                return
            }
            proxies[profile] = proxy
            publishConnectedDevices()
        }

        override fun onServiceDisconnected(profile: Int) {
            proxies.remove(profile)
            publishConnectedDevices()
        }
    }

    private val connectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            publishConnectedDevices()
        }
    }

    /** Starts watching. [onDevicesChanged] receives the full list now and after every change. */
    fun start(onDevicesChanged: (List<AudioDevice>) -> Unit) {
        if (adapter == null || listener != null || !hasPermission()) return
        listener = onDevicesChanged
        watchedProfiles.keys.forEach { profile -> adapter.getProfileProxy(context, profileListener, profile) }
        val filter = IntentFilter().apply { watchedProfiles.values.forEach { addAction(it) } }
        context.registerReceiver(connectionReceiver, filter)
    }

    fun stop() {
        if (listener == null) return
        context.unregisterReceiver(connectionReceiver)
        proxies.forEach { (profile, proxy) -> adapter?.closeProfileProxy(profile, proxy) }
        proxies.clear()
        listener = null
    }

    // MissingPermission: the permission is checked right here, through hasPermission().
    // InlinedApi: LE_AUDIO is only a number; on Android 12 and older nothing watches it, so its list is empty.
    @SuppressLint("MissingPermission", "InlinedApi")
    private fun publishConnectedDevices() {
        val callback = listener ?: return
        if (!hasPermission()) {
            callback(emptyList())
            return
        }
        val connectedByProfile = proxies.mapValues { (_, proxy) -> proxy.connectedDevices }
        val headsetDevices = connectedByProfile[BluetoothProfile.HEADSET].orEmpty()
        val a2dpDevices = connectedByProfile[BluetoothProfile.A2DP].orEmpty()
        // LE Audio is one profile for both calls and media, unlike classic Bluetooth's HEADSET + A2DP.
        val leAudioDevices = connectedByProfile[BluetoothProfile.LE_AUDIO].orEmpty()
        val devices = connectedByProfile.values
            .flatten()
            .distinct()
            .map { device ->
                val isLeAudio = device in leAudioDevices
                device.toAudioDevice(
                    callConnected = device in headsetDevices || isLeAudio,
                    mediaConnected = device in a2dpDevices || isLeAudio,
                    isLeAudio = isLeAudio
                )
            }
            .sortedBy { it.name.lowercase() }
        Log.d(TAG, "Connected devices (${devices.size}): $devices")
        callback(devices)
    }

    companion object {
        private const val TAG = "ConnectedDevicesDetector"

        /** Picks the detector for the Android version the app is running on. */
        fun create(context: Context): ConnectedDevicesDetector =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ModernConnectedDevicesDetector(context)
            } else {
                LegacyConnectedDevicesDetector(context)
            }
    }
}

/** Service ids a device advertises when it can take calls: Hands-Free (HFP) and Headset (HSP). */
private val CALL_SERVICES = setOf(
    ParcelUuid.fromString("0000111E-0000-1000-8000-00805F9B34FB"),
    ParcelUuid.fromString("00001108-0000-1000-8000-00805F9B34FB")
)

/** Service id a device advertises when it can play media: A2DP Audio Sink. */
private val MEDIA_SERVICES = setOf(ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB"))

// Only called from publishConnectedDevices(), after the permission check.
@SuppressLint("MissingPermission")
private fun BluetoothDevice.toAudioDevice(
    callConnected: Boolean,
    mediaConnected: Boolean,
    isLeAudio: Boolean
): AudioDevice {
    // The services the device advertised when it was paired; null if Android hasn't read them yet.
    val services = uuids?.toSet()
    return AudioDevice(
        name = alias ?: name ?: address,
        type = when (bluetoothClass?.deviceClass) {
            BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO,
            BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE -> AudioDeviceType.CAR
            else -> AudioDeviceType.HEADSET
        },
        address = address,
        calls = profileState(callConnected, services, CALL_SERVICES),
        media = profileState(mediaConnected, services, MEDIA_SERVICES),
        isLeAudio = isLeAudio
    )
}

/**
 * Connected wins. Otherwise the device's advertised services tell whether it offers this kind of
 * audio at all. If Android hasn't read them yet, support is assumed rather than guessing "not supported".
 */
private fun profileState(
    connected: Boolean,
    services: Set<ParcelUuid>?,
    profileServices: Set<ParcelUuid>
): ProfileState = when {
    connected -> ProfileState.CONNECTED
    services == null || services.any { it in profileServices } -> ProfileState.NOT_CONNECTED
    else -> ProfileState.NOT_SUPPORTED
}
