package com.example.bluetoothtestapp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.media.AudioDeviceInfo

// Readable descriptions of Android's device objects, for Logcat.

/** For example `BLUETOOTH_SCO address=00:00:00:00:00:00 product=Phone`. */
@SuppressLint("InlinedApi")
fun AudioDeviceInfo.describe(): String {
    val typeName = when (type) {
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "BLUETOOTH_SCO"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "BLUETOOTH_A2DP"
        AudioDeviceInfo.TYPE_BLE_HEADSET -> "BLE_HEADSET"
        AudioDeviceInfo.TYPE_BLE_SPEAKER -> "BLE_SPEAKER"
        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "BUILTIN_EARPIECE"
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "BUILTIN_SPEAKER"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "WIRED_HEADSET"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "WIRED_HEADPHONES"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB_HEADSET"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB_DEVICE"
        else -> "TYPE_$type"
    }
    return "$typeName address=$address product=$productName"
}

/**
 * For example `Headset (00:11:22:33:44:55)`, or `none`. Reading the name needs BLUETOOTH_CONNECT on
 * Android 12+; without it only the address is shown.
 */
@SuppressLint("MissingPermission")
fun BluetoothDevice?.describe(): String {
    if (this == null) return "none"
    val displayName = try {
        alias ?: name
    } catch (e: SecurityException) {
        null
    }
    return "${displayName ?: "?"} ($address)"
}
