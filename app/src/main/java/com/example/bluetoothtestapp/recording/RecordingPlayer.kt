package com.example.bluetoothtestapp.recording

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import java.io.File

/**
 * Plays a recording with [MediaPlayer] as media audio. Android routes media to the active
 * Bluetooth media (A2DP) device, or to the host's speaker when no Bluetooth device is active.
 */
class RecordingPlayer {

    private var player: MediaPlayer? = null

    /**
     * Plays [file] from the start.
     *
     * @param onFinished called when playback ends, fails or can't start.
     */
    fun play(file: File, onFinished: () -> Unit) {
        stop()
        val newPlayer = MediaPlayer()
        try {
            newPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            newPlayer.setDataSource(file.absolutePath)
            newPlayer.setOnCompletionListener {
                stop()
                onFinished()
            }
            newPlayer.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "Playback error what=$what extra=$extra")
                stop()
                onFinished()
                true
            }
            newPlayer.prepare()
            newPlayer.start()
            player = newPlayer
            Log.d(TAG, "Playback started")
        } catch (e: Exception) {
            Log.e(TAG, "Playback could not start", e)
            newPlayer.release()
            onFinished()
        }
    }

    /** Stops playback, if it is running, and frees the player. */
    fun stop() {
        player?.release()
        player = null
    }

    private companion object {
        const val TAG = "RecordingPlayer"
    }
}
