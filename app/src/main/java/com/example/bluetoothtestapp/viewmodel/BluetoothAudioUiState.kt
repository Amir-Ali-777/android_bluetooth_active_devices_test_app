package com.example.bluetoothtestapp.viewmodel

import com.example.bluetoothtestapp.model.AudioDevice
import com.example.bluetoothtestapp.model.ProfileState

/** What a device card shows for calls and for media. */
enum class RouteStatus {
    /** Android sends this kind of audio to the device. */
    ACTIVE,

    /** The device supports this kind of audio, but Android sends it elsewhere. */
    INACTIVE,

    /** The device can't handle this kind of audio at all. */
    NOT_SUPPORTED
}

/**
 * Everything the screen shows.
 *
 * @property unknownHeadset shown as the communication device when a Bluetooth headset is active
 *   but Android doesn't say which one.
 * @property bluetoothDevices the connected Bluetooth audio devices.
 * @property wiredDevices the plugged-in wired audio devices (3.5 mm jack and USB).
 * @property hasBluetoothCommunicationDevice Android has a Bluetooth headset (classic or LE Audio)
 *   active for two-way audio.
 * @property activeCommunicationAddress that headset's address, or null if Android doesn't name it.
 * @property activeMediaAddress the Bluetooth device Android plays media through, or null.
 * @property wiredMediaDeviceId the [AudioDevice.id] of the wired device media plays through, or null.
 */
data class BluetoothAudioUiState(
    val hostDevice: AudioDevice,
    val unknownHeadset: AudioDevice,
    val bluetoothDevices: List<AudioDevice> = emptyList(),
    val wiredDevices: List<AudioDevice> = emptyList(),
    val hasBluetoothCommunicationDevice: Boolean = false,
    val activeCommunicationAddress: String? = null,
    val activeMediaAddress: String? = null,
    val wiredMediaDeviceId: String? = null,
    val isScoActive: Boolean = false,
    val hasRecording: Boolean = false,
    val isPlayingRecording: Boolean = false
) {
    /** Bluetooth and wired devices together. */
    val connectedDevices: List<AudioDevice>
        get() = bluetoothDevices + wiredDevices

    /** The host card is shown only while no device is connected. */
    val displayedDevices: List<AudioDevice>
        get() = connectedDevices.ifEmpty { listOf(hostDevice) }

    /**
     * The active communication device: the one two-way audio (calls, SCO) goes to. It doesn't
     * depend on media, which may play somewhere else.
     *
     * 1. An active Bluetooth headset, because calls and Start SCO use it.
     *    Connected headsets don't count unless Android has made one of them active. Some devices
     *    can't name the active headset until it changes or carries SCO, e.g. right
     *    after the app starts. Then the only headset connected for calls must be the one; with
     *    several, [unknownHeadset] is shown rather than a guess.
     * 2. Otherwise a wired device, which Android uses instead of the earpiece and built-in
     *    microphone as soon as it is plugged in.
     * 3. Otherwise the host.
     */
    val activeCommunicationDevice: AudioDevice
        get() {
            if (hasBluetoothCommunicationDevice) {
                return bluetoothDevice(activeCommunicationAddress)
                    ?: bluetoothDevices.singleOrNull { it.calls == ProfileState.CONNECTED }
                    ?: unknownHeadset
            }
            return wiredDevices.lastOrNull() ?: hostDevice
        }

    /** The microphone two-way audio records from: the communication device's own, or the host's built-in one. */
    val communicationMicrophone: AudioDevice
        get() = activeCommunicationDevice.takeIf { it.hasMicrophone } ?: hostDevice

    /** The device media plays through (Android Settings marks a Bluetooth one "Active"), otherwise the host. */
    val activeMediaDevice: AudioDevice
        get() = wiredDevices.firstOrNull { it.id == wiredMediaDeviceId }
            ?: bluetoothDevice(activeMediaAddress)
            ?: hostDevice

    /** The "Call" row of [device]'s card. */
    fun callStatus(device: AudioDevice): RouteStatus = routeStatus(device.calls, device == activeCommunicationDevice)

    /** The "Media" row of [device]'s card. */
    fun mediaStatus(device: AudioDevice): RouteStatus = routeStatus(device.media, device == activeMediaDevice)

    /** Start SCO: a Bluetooth headset is the communication device, no session is running and nothing is playing. */
    val isStartScoEnabled: Boolean
        get() = hasBluetoothCommunicationDevice && !isScoActive && !isPlayingRecording

    /** Stop SCO: enabled whenever a Bluetooth headset is the communication device, even with no session, for experiments. */
    val isStopScoEnabled: Boolean
        get() = hasBluetoothCommunicationDevice

    /** Play Recording: a recording exists (it can play through the host too) and isn't playing already. */
    val isPlayRecordingEnabled: Boolean
        get() = hasRecording && !isPlayingRecording

    private fun bluetoothDevice(address: String?): AudioDevice? =
        if (address == null) null else bluetoothDevices.firstOrNull { it.address.equals(address, ignoreCase = true) }

    private fun routeStatus(state: ProfileState, isActive: Boolean): RouteStatus = when {
        state == ProfileState.NOT_SUPPORTED -> RouteStatus.NOT_SUPPORTED
        isActive -> RouteStatus.ACTIVE
        else -> RouteStatus.INACTIVE
    }
}
