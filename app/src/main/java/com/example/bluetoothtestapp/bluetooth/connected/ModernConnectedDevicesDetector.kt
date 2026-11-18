package com.example.bluetoothtestapp.bluetooth.connected

import android.Manifest
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothLeAudio
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Android 12+ (API 31+). Every Bluetooth call needs the BLUETOOTH_CONNECT runtime permission.
 * From Android 13 it also watches LE Audio, the newer profile that LE Audio earbuds use instead
 * of HEADSET/A2DP.
 */
@RequiresApi(Build.VERSION_CODES.S)
class ModernConnectedDevicesDetector(context: Context) : ConnectedDevicesDetector(context) {

    override val watchedProfiles = buildMap {
        put(BluetoothProfile.HEADSET, BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        put(BluetoothProfile.A2DP, BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            put(BluetoothProfile.LE_AUDIO, BluetoothLeAudio.ACTION_LE_AUDIO_CONNECTION_STATE_CHANGED)
        }
    }

    override fun hasPermission() =
        context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
}
