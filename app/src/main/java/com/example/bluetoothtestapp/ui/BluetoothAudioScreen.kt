package com.example.bluetoothtestapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bluetoothtestapp.R
import com.example.bluetoothtestapp.model.AudioDevice
import com.example.bluetoothtestapp.model.AudioDeviceType
import com.example.bluetoothtestapp.model.ProfileState
import com.example.bluetoothtestapp.ui.components.ActiveDeviceCard
import com.example.bluetoothtestapp.ui.components.ControlButton
import com.example.bluetoothtestapp.ui.components.DeviceCard
import com.example.bluetoothtestapp.ui.theme.BackgroundBottom
import com.example.bluetoothtestapp.ui.theme.BackgroundLower
import com.example.bluetoothtestapp.ui.theme.BackgroundMiddle
import com.example.bluetoothtestapp.ui.theme.BackgroundTop
import com.example.bluetoothtestapp.ui.theme.BluetoothTestAppTheme
import com.example.bluetoothtestapp.ui.theme.InfoIconTint
import com.example.bluetoothtestapp.ui.theme.TextLavender
import com.example.bluetoothtestapp.viewmodel.BluetoothAudioUiState

private val ScreenPadding = 24.dp

private val ScreenBackground = Brush.linearGradient(
    0.0f to BackgroundTop,
    0.55f to BackgroundMiddle,
    0.85f to BackgroundLower,
    1.0f to BackgroundBottom
)

/**
 * The whole screen. It only draws [uiState] and reports button taps through the three callbacks;
 * all decisions are made by the ViewModel.
 */
@Composable
fun BluetoothAudioScreen(
    uiState: BluetoothAudioUiState,
    modifier: Modifier = Modifier,
    onStartScoClick: () -> Unit = {},
    onStopScoClick: () -> Unit = {},
    onPlayRecordingClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        Header(modifier = Modifier.padding(horizontal = ScreenPadding))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.connected_devices, uiState.connectedDevices.size),
            style = MaterialTheme.typography.titleMedium,
            color = TextLavender,
            modifier = Modifier.padding(horizontal = ScreenPadding)
        )
        Spacer(modifier = Modifier.height(32.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.displayedDevices, key = { it.id }) { device ->
                DeviceCard(
                    device = device,
                    callStatus = uiState.callStatus(device),
                    mediaStatus = uiState.mediaStatus(device),
                    modifier = Modifier.animateItem()
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        AudioControls(
            uiState = uiState,
            onStartScoClick = onStartScoClick,
            onStopScoClick = onStopScoClick,
            onPlayRecordingClick = onPlayRecordingClick,
            modifier = Modifier.padding(horizontal = ScreenPadding)
        )
        Spacer(modifier = Modifier.height(12.dp))
        ActiveDeviceCard(
            communicationDevice = uiState.activeCommunicationDevice,
            microphone = uiState.communicationMicrophone,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.screen_title),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(R.drawable.ic_info_outline),
            contentDescription = stringResource(R.string.app_info),
            tint = InfoIconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun AudioControls(
    uiState: BluetoothAudioUiState,
    onStartScoClick: () -> Unit,
    onStopScoClick: () -> Unit,
    onPlayRecordingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.audio_controls),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ControlButton(
                text = stringResource(R.string.start_sco),
                enabled = uiState.isStartScoEnabled,
                onClick = onStartScoClick,
                highlighted = uiState.isScoActive,
                modifier = Modifier.weight(1f)
            )
            ControlButton(
                text = stringResource(R.string.stop_sco),
                enabled = uiState.isStopScoEnabled,
                onClick = onStopScoClick,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        ControlButton(
            text = stringResource(R.string.play_recording),
            enabled = uiState.isPlayRecordingEnabled,
            onClick = onPlayRecordingClick,
            highlighted = uiState.isPlayingRecording,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private val PreviewUnknownHeadset = AudioDevice(name = "Unknown Bluetooth headset", type = AudioDeviceType.HEADSET)

@Preview(name = "Host only (what the app shows now)", widthDp = 360, heightDp = 720)
@Composable
private fun HostOnlyPreview() {
    BluetoothTestAppTheme {
        BluetoothAudioScreen(
            uiState = BluetoothAudioUiState(
                hostDevice = AudioDevice(name = "Phone", type = AudioDeviceType.HOST),
                unknownHeadset = PreviewUnknownHeadset
            )
        )
    }
}

@Preview(name = "LE Audio buds for calls, media on a speaker", widthDp = 360, heightDp = 720)
@Composable
private fun BluetoothConnectedPreview() {
    val buds = AudioDevice(
        name = "LE Audio buds",
        type = AudioDeviceType.HEADSET,
        address = "00:11:22:33:44:55",
        isLeAudio = true
    )
    val callOnlyHeadset = AudioDevice(
        name = "Call headset",
        type = AudioDeviceType.HEADSET,
        address = "66:77:88:99:AA:BB",
        media = ProfileState.NOT_SUPPORTED
    )
    val speaker = AudioDevice(
        name = "Speaker",
        type = AudioDeviceType.HEADSET,
        address = "AA:BB:CC:DD:EE:FF",
        calls = ProfileState.NOT_SUPPORTED
    )
    BluetoothTestAppTheme {
        BluetoothAudioScreen(
            uiState = BluetoothAudioUiState(
                hostDevice = AudioDevice(name = "Phone", type = AudioDeviceType.HOST),
                unknownHeadset = PreviewUnknownHeadset,
                bluetoothDevices = listOf(buds, callOnlyHeadset, speaker),
                hasBluetoothCommunicationDevice = true,
                activeCommunicationAddress = buds.address,
                activeMediaAddress = speaker.address,
                isScoActive = true,
                hasRecording = true
            )
        )
    }
}

@Preview(name = "Wired headphones: calls use the built-in mic", widthDp = 360, heightDp = 720)
@Composable
private fun WiredPreview() {
    val headphones = AudioDevice(
        name = "Wired headphones",
        type = AudioDeviceType.WIRED,
        hasMicrophone = false,
        id = "wired:4:"
    )
    BluetoothTestAppTheme {
        BluetoothAudioScreen(
            uiState = BluetoothAudioUiState(
                hostDevice = AudioDevice(name = "Phone", type = AudioDeviceType.HOST),
                unknownHeadset = PreviewUnknownHeadset,
                wiredDevices = listOf(headphones),
                wiredMediaDeviceId = headphones.id
            )
        )
    }
}
