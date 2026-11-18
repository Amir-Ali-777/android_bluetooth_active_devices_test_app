package com.example.bluetoothtestapp.model

enum class AudioDeviceType {
    HOST,
    HEADSET,
    CAR,

    /** Plugged into the 3.5 mm jack: a headset with a microphone, or headphones without one. */
    WIRED,

    /** Plugged into the USB port: a USB headset, or a USB-C audio adapter. */
    USB
}

/** How a device relates to one kind of audio: calls or media. */
enum class ProfileState {
    /** The device doesn't offer this kind of audio at all, e.g. calls on a Bluetooth speaker. */
    NOT_SUPPORTED,

    /** The device offers it, but it isn't connected now, e.g. turned off in its Bluetooth settings. */
    NOT_CONNECTED,

    /** Connected for this kind of audio. */
    CONNECTED
}

/**
 * A device that can play audio.
 *
 * @property address the Bluetooth MAC address; the host and wired devices have none.
 * @property calls two-way call audio with the device's microphone: the Headset profile (HFP) on
 *   classic Bluetooth, or the LE Audio profile. Wired devices can always carry call audio.
 * @property media one-way, high-quality media audio: A2DP on classic Bluetooth, or LE Audio.
 *   The host handles both kinds of audio itself, hence the defaults.
 * @property isLeAudio connected over LE Audio (Android 13+) instead of classic Bluetooth.
 * @property hasMicrophone false for output-only devices such as wired headphones; two-way audio
 *   then uses the host's built-in microphone.
 * @property id identifies the device in lists: the Bluetooth address, or the wired port.
 */
data class AudioDevice(
    val name: String,
    val type: AudioDeviceType,
    val address: String? = null,
    val calls: ProfileState = ProfileState.CONNECTED,
    val media: ProfileState = ProfileState.CONNECTED,
    val isLeAudio: Boolean = false,
    val hasMicrophone: Boolean = true,
    val id: String = address ?: name
)
