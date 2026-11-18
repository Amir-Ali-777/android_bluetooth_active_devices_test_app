package com.example.bluetoothtestapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.bluetoothtestapp.ui.BluetoothAudioScreen
import com.example.bluetoothtestapp.ui.theme.BluetoothTestAppTheme
import com.example.bluetoothtestapp.viewmodel.BluetoothAudioViewModel

/**
 * The only screen of the app. It asks for the runtime permissions, starts device detection
 * once Bluetooth may be used, and connects the screen to the ViewModel.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: BluetoothAudioViewModel by viewModels()

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (hasBluetoothPermission()) viewModel.startDeviceDetection()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The screen is always dark, so system bar icons must stay light even in light mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            BluetoothTestAppTheme {
                val uiState by viewModel.uiState.collectAsState()
                BluetoothAudioScreen(
                    uiState = uiState,
                    onStartScoClick = viewModel::onStartScoClick,
                    onStopScoClick = viewModel::onStopScoClick,
                    onPlayRecordingClick = viewModel::onPlayRecordingClick
                )
            }
        }

        if (hasBluetoothPermission()) viewModel.startDeviceDetection()
        val missingPermissions = runtimePermissions().filter { !isGranted(it) }
        // Ask only on a fresh start, not again after every screen rotation.
        if (missingPermissions.isNotEmpty() && savedInstanceState == null) {
            permissionRequest.launch(missingPermissions.toTypedArray())
        }
    }

    /** Microphone on every version, plus Bluetooth on Android 12+ (Android 11 grants it at install). */
    private fun runtimePermissions(): List<String> = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(Manifest.permission.BLUETOOTH_CONNECT)
    }

    private fun hasBluetoothPermission() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || isGranted(Manifest.permission.BLUETOOTH_CONNECT)

    private fun isGranted(permission: String) =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}