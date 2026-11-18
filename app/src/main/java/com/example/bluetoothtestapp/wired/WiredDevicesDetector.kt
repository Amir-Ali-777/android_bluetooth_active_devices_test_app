package com.example.bluetoothtestapp.wired

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.example.bluetoothtestapp.R
import com.example.bluetoothtestapp.bluetooth.describe
import com.example.bluetoothtestapp.model.AudioDevice
import com.example.bluetoothtestapp.model.AudioDeviceType

/**
 * Detects wired audio devices: 3.5 mm headsets and headphones, and USB headsets and adapters.
 *
 * Wired devices are not Bluetooth: there are no profiles, proxies or Bluetooth broadcasts. They
 * are plain audio devices in [AudioManager], listed by [AudioManager.getDevices] and reported by
 * [AudioDeviceCallback] whenever one is plugged in or out. Those APIs are the same on Android 11
 * and 12+, so one class serves every version; only the media check differs ([findMediaOutput]).
 *
 * A wired device has a microphone when AudioManager also lists it as an input. No permission is
 * needed. Everything is logged with the tag `WiredDevicesDetector`.
 */
class WiredDevicesDetector(context: Context) {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var listener: ((WiredDevices) -> Unit)? = null

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) = publishWiredDevices()
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) = publishWiredDevices()
    }

    /** Starts watching. [onWiredDevicesChanged] receives the wired devices now and after every change. */
    fun start(onWiredDevicesChanged: (WiredDevices) -> Unit) {
        if (listener != null) return
        listener = onWiredDevicesChanged
        audioManager.registerAudioDeviceCallback(deviceCallback, null)
        publishWiredDevices()
    }

    fun stop() {
        if (listener == null) return
        audioManager.unregisterAudioDeviceCallback(deviceCallback)
        listener = null
    }

    private fun publishWiredDevices() {
        val callback = listener ?: return
        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .filter { it.type in WIRED_TYPES }
            .distinctBy { it.type to it.address }
        val inputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            .filter { it.type in WIRED_TYPES }
        val devices = outputs.map { output ->
            output.toAudioDevice(hasMicrophone = inputs.any { it.address == output.address })
        }
        val mediaOutput = findMediaOutput(outputs)
        Log.d(TAG, "Wired devices (${devices.size}): $devices | media plays on wired: ${mediaOutput?.describe() ?: "no"}")
        callback(WiredDevices(devices, mediaDeviceId = mediaOutput?.wiredId))
    }

    /**
     * The wired device media plays through, or null if media plays elsewhere.
     *
     * Android 13+ says exactly where media goes ([AudioManager.getAudioDevicesForAttributes]).
     * Android 11 and 12 can't be asked. There media goes to whichever of the wired device and the
     * Bluetooth media device was connected last, and the app assumes the wired device, which is
     * right whenever it was plugged in after the Bluetooth device connected.
     */
    private fun findMediaOutput(wiredOutputs: List<AudioDeviceInfo>): AudioDeviceInfo? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return wiredOutputs.lastOrNull()
        val mediaRoute = audioManager.getAudioDevicesForAttributes(MEDIA_ATTRIBUTES)
        return wiredOutputs.firstOrNull { output ->
            mediaRoute.any { it.type == output.type && it.address == output.address }
        }
    }

    private fun AudioDeviceInfo.toAudioDevice(hasMicrophone: Boolean) = AudioDevice(
        name = displayName(),
        type = if (type in JACK_TYPES) AudioDeviceType.WIRED else AudioDeviceType.USB,
        hasMicrophone = hasMicrophone,
        id = wiredId
    )

    /** 3.5 mm devices report the phone's model as their product name, so they get a generic one. */
    private fun AudioDeviceInfo.displayName(): String = when (type) {
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> appContext.getString(R.string.wired_headset)
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> appContext.getString(R.string.wired_headphones)
        else -> productName?.toString().orEmpty().ifBlank { appContext.getString(R.string.usb_audio_device) }
    }

    /** Stable while the device stays plugged in; the address tells USB devices apart. */
    private val AudioDeviceInfo.wiredId: String
        get() = "wired:$type:$address"

    private companion object {
        const val TAG = "WiredDevicesDetector"

        /** The 3.5 mm jack: with a microphone (headset) or without (headphones). */
        val JACK_TYPES = setOf(AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES)

        /** The jack plus USB: headsets, and other USB audio devices such as USB-C adapters. */
        val WIRED_TYPES = JACK_TYPES + setOf(AudioDeviceInfo.TYPE_USB_HEADSET, AudioDeviceInfo.TYPE_USB_DEVICE)

        val MEDIA_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }
}
