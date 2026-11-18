package com.example.bluetoothtestapp.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

/**
 * Records audio into a single file in the app's cache folder, so only the latest recording
 * exists. It uses [MediaRecorder] with the VOICE_COMMUNICATION source: while SCO is up, Android
 * takes that source from the Bluetooth headset's microphone instead of the host's.
 *
 * The file is 16 kHz mono AAC (`.m4a`), which matches the call-quality audio SCO delivers.
 */
class AudioRecorder(private val context: Context) {

    /** The recording. It exists only after a recording has been saved. */
    val file = File(context.cacheDir, "sco_recording.m4a")

    private var recorder: MediaRecorder? = null

    /**
     * Starts recording and deletes any earlier recording.
     *
     * @return false if recording could not start, for example because the microphone
     *   permission is missing.
     */
    fun start(): Boolean {
        deleteRecording()
        val newRecorder = createMediaRecorder()
        return try {
            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16_000)
                setAudioChannels(1)
                setAudioEncodingBitRate(32_000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = newRecorder
            Log.d(TAG, "Recording started")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Recording could not start", e)
            newRecorder.release()
            false
        }
    }

    /**
     * Stops recording and keeps the file.
     *
     * @return true if a playable recording was saved; false if nothing was being recorded.
     */
    fun stop(): Boolean {
        val activeRecorder = recorder ?: return false
        recorder = null
        return try {
            activeRecorder.stop()
            Log.d(TAG, "Recording saved: ${file.length()} bytes")
            true
        } catch (e: RuntimeException) {
            // MediaRecorder.stop() throws if no audio was captured yet, e.g. when stopped right after starting.
            Log.w(TAG, "Nothing was recorded", e)
            deleteRecording()
            false
        } finally {
            activeRecorder.release()
        }
    }

    /** Deletes the recording, if there is one. */
    fun deleteRecording() {
        file.delete()
    }

    private fun createMediaRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()

    private companion object {
        const val TAG = "AudioRecorder"
    }
}
