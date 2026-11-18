package com.example.bluetoothtestapp.wired

import com.example.bluetoothtestapp.model.AudioDevice

/**
 * What [WiredDevicesDetector] found.
 *
 * @property devices the wired audio devices plugged in now.
 * @property mediaDeviceId the [AudioDevice.id] of the one media plays through, or null when media
 *   plays elsewhere (a Bluetooth device or the host).
 */
data class WiredDevices(
    val devices: List<AudioDevice>,
    val mediaDeviceId: String?
)
