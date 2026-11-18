package com.example.bluetoothtestapp.bluetooth.active

/**
 * What [ActiveDeviceResolver] found.
 *
 * @property hasBluetoothCommunicationDevice a Bluetooth headset (classic or LE Audio) is active
 *   for two-way audio. False means two-way audio stays on the host, even if headsets are
 *   connected, e.g. after the active headset is switched off and Android doesn't pick another.
 * @property communicationAddress that headset's address, or null if Android doesn't name it.
 *   Some devices don't until the headset changes, e.g. right after the app starts.
 * @property mediaAddress the device media plays through, or null for the host.
 */
data class ActiveDevices(
    val hasBluetoothCommunicationDevice: Boolean,
    val communicationAddress: String?,
    val mediaAddress: String?
)
