package com.example.bluetoothtestapp.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothtestapp.R
import com.example.bluetoothtestapp.bluetooth.active.ActiveDeviceResolver
import com.example.bluetoothtestapp.bluetooth.connected.ConnectedDevicesDetector
import com.example.bluetoothtestapp.bluetooth.sco.ScoConnectionManager
import com.example.bluetoothtestapp.model.AudioDevice
import com.example.bluetoothtestapp.model.AudioDeviceType
import com.example.bluetoothtestapp.recording.AudioRecorder
import com.example.bluetoothtestapp.recording.RecordingPlayer
import com.example.bluetoothtestapp.wired.WiredDevicesDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the screen state and connects the screen to the business logic.
 *
 * - Device detection: [ConnectedDevicesDetector] and [ActiveDeviceResolver] keep the Bluetooth
 *   devices and the active communication and media devices up to date; [WiredDevicesDetector]
 *   does the same for wired devices.
 * - Start SCO: opens SCO with [ScoConnectionManager], records [RECORDING_DURATION_MS] with
 *   [AudioRecorder] once the link is up, then closes SCO and keeps the recording.
 * - Stop SCO: ends that early, or just calls Android's stop API when no session is running.
 * - Play Recording: plays the latest recording with [RecordingPlayer].
 *
 * The recording lives only while the app is open: it is deleted when the app starts and when
 * this ViewModel is destroyed.
 */
class BluetoothAudioViewModel(application: Application) : AndroidViewModel(application) {

    private val connectedDevicesDetector = ConnectedDevicesDetector.create(application)
    private val activeDeviceResolver = ActiveDeviceResolver.create(application)
    private val wiredDevicesDetector = WiredDevicesDetector(application)
    private val scoConnectionManager = ScoConnectionManager.create(application)
    private val audioRecorder = AudioRecorder(application)
    private val recordingPlayer = RecordingPlayer()

    /** Ends the SCO session when SCO doesn't come up in time, or when the recording time is over. */
    private var scoSessionTimer: Job? = null

    private val _uiState = MutableStateFlow(
        BluetoothAudioUiState(
            hostDevice = AudioDevice(name = Build.MODEL, type = AudioDeviceType.HOST),
            unknownHeadset = AudioDevice(
                name = application.getString(R.string.unknown_headset),
                type = AudioDeviceType.HEADSET
            )
        )
    )
    val uiState: StateFlow<BluetoothAudioUiState> = _uiState.asStateFlow()

    init {
        // A recording left over from an earlier run is not shown: the app always opens without one.
        audioRecorder.deleteRecording()
        // Wired devices need no permission, so they are watched straight away.
        wiredDevicesDetector.start { wired ->
            _uiState.update { it.copy(wiredDevices = wired.devices, wiredMediaDeviceId = wired.mediaDeviceId) }
            logActiveDevices()
        }
    }

    /**
     * Watches connected and active Bluetooth devices, which needs the Bluetooth permission. Safe to
     * call more than once; it starts only the first time.
     */
    fun startDeviceDetection() {
        connectedDevicesDetector.start { devices ->
            _uiState.update { it.copy(bluetoothDevices = devices) }
            logActiveDevices()
        }
        activeDeviceResolver.start { active ->
            _uiState.update {
                it.copy(
                    hasBluetoothCommunicationDevice = active.hasBluetoothCommunicationDevice,
                    activeCommunicationAddress = active.communicationAddress,
                    activeMediaAddress = active.mediaAddress
                )
            }
            logActiveDevices()
        }
    }

    /** Logs what the screen shows as active, so it can be compared with what Android really does. */
    private fun logActiveDevices() {
        val state = _uiState.value
        Log.d(
            TAG,
            "Shown as active: communication = ${state.activeCommunicationDevice}, " +
                "microphone = ${state.communicationMicrophone.name}, media = ${state.activeMediaDevice}"
        )
    }

    /**
     * Start SCO: deletes the previous recording, opens SCO to the active headset and starts
     * recording once the link is up. The session ends on its own after [RECORDING_DURATION_MS],
     * or after [SCO_CONNECT_TIMEOUT_MS] if SCO never comes up.
     */
    fun onStartScoClick() {
        if (!_uiState.value.isStartScoEnabled) return
        Log.d(TAG, "Start SCO: expected on ${_uiState.value.activeCommunicationDevice}")
        audioRecorder.deleteRecording()
        _uiState.update { it.copy(isScoActive = true, hasRecording = false) }

        // Started before the request, because SCO can report "connected" while start() is still running.
        scoSessionTimer = viewModelScope.launch {
            delay(SCO_CONNECT_TIMEOUT_MS)
            Log.w(TAG, "SCO did not connect within $SCO_CONNECT_TIMEOUT_MS ms")
            stopScoAndSaveRecording()
        }
        val accepted = scoConnectionManager.start(
            onConnected = ::startRecording,
            onDisconnected = ::stopScoAndSaveRecording
        )
        if (!accepted) {
            Log.w(TAG, "Android rejected the SCO request")
            stopScoAndSaveRecording()
        }
    }

    /**
     * Stop SCO: ends a running session early and keeps what was recorded. When no session is
     * running, Android's stop API is still called, so you can see how the platform reacts.
     */
    fun onStopScoClick() {
        Log.d(TAG, "Stop SCO: session running = ${_uiState.value.isScoActive}")
        stopScoAndSaveRecording()
    }

    /** Play Recording: plays the latest recording through the active audio device. */
    fun onPlayRecordingClick() {
        if (!_uiState.value.isPlayRecordingEnabled) return
        Log.d(TAG, "Play Recording: expected on ${_uiState.value.activeMediaDevice}")
        _uiState.update { it.copy(isPlayingRecording = true) }
        recordingPlayer.play(audioRecorder.file) {
            _uiState.update { it.copy(isPlayingRecording = false) }
        }
    }

    /** Runs when SCO is up: records from the headset microphone for [RECORDING_DURATION_MS]. */
    private fun startRecording() {
        scoSessionTimer?.cancel()
        if (!audioRecorder.start()) {
            stopScoAndSaveRecording()
            return
        }
        scoSessionTimer = viewModelScope.launch {
            delay(RECORDING_DURATION_MS)
            stopScoAndSaveRecording()
        }
    }

    /** Ends the SCO session: stops recording (keeping the file), closes SCO and updates the buttons. */
    private fun stopScoAndSaveRecording() {
        scoSessionTimer?.cancel()
        scoSessionTimer = null
        val saved = audioRecorder.stop()
        scoConnectionManager.stop()
        _uiState.update { it.copy(isScoActive = false, hasRecording = it.hasRecording || saved) }
    }

    override fun onCleared() {
        connectedDevicesDetector.stop()
        activeDeviceResolver.stop()
        wiredDevicesDetector.stop()
        if (_uiState.value.isScoActive) stopScoAndSaveRecording()
        recordingPlayer.stop()
        audioRecorder.deleteRecording()
    }

    private companion object {
        const val TAG = "BluetoothAudioViewModel"

        /** How long the headset microphone is recorded after SCO comes up. */
        const val RECORDING_DURATION_MS = 10_000L

        /** How long to wait for Android to bring SCO up before giving up. */
        const val SCO_CONNECT_TIMEOUT_MS = 5_000L
    }
}
