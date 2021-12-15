package com.hedvig.app.feature.embark.passages.audiorecorder

import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.tracking.TrackingFacade
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.time.Clock
import java.time.Instant
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class AudioRecorderViewModel(
    private val clock: Clock,
    private val trackingFacade: TrackingFacade,
) : ViewModel() {
    sealed class ViewState {
        object NotRecording : ViewState()
        data class Recording(
            val amplitudes: List<Int>,
            val startedAt: Instant,
            val filePath: String,
        ) : ViewState()

        data class Playback(
            val filePath: String,
            val isPlaying: Boolean,
            val isPrepared: Boolean,
            val amplitudes: List<Int>,
            val progress: Float,
        ) : ViewState()
    }

    private var recorder: MediaRecorder? = null
    private var timer: Timer? = null
    private var player: MediaPlayer? = null

    private val _viewState = MutableStateFlow<ViewState>(ViewState.NotRecording)
    val viewState = _viewState.asStateFlow()

    fun startRecording() {
        if (recorder == null) {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setAudioSamplingRate(96_000)
                setAudioEncodingBitRate(128_000)
                val filePath = File.createTempFile(
                    "claim_${UUID.randomUUID()}",
                    ".aac",
                ).absolutePath
                setOutputFile(filePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
                start()
                _viewState.value = ViewState.Recording(emptyList(), Instant.now(clock), filePath)
            }
            timer = Timer()
            timer?.schedule(
                timerTask {
                    recorder?.maxAmplitude?.let { amplitude ->
                        _viewState.update { vs ->
                            if (vs is ViewState.Recording) {
                                vs.copy(amplitudes = vs.amplitudes + amplitude)
                            } else {
                                vs
                            }
                        }
                    }
                },
                0,
                1000L / 60,
            )
            trackingFacade.track("begin_recording")
        }
    }

    fun stopRecording() {
        val currentState = viewState.value
        if (currentState !is ViewState.Recording) {
            throw IllegalStateException("Must be in Recording-state to stop recording")
        }
        cleanup()
        player = MediaPlayer().apply {
            setDataSource(currentState.filePath)
            setOnPreparedListener {
                _viewState.value = ViewState.Playback(
                    filePath = currentState.filePath,
                    isPlaying = false,
                    isPrepared = true,
                    amplitudes = currentState.amplitudes,
                    progress = 0f,
                )
            }
            setOnCompletionListener {
                // Bail if the user has backed out of the playback-state
                val currentPlaybackState = viewState.value as? ViewState.Playback ?: return@setOnCompletionListener
                cleanupTimer()
                _viewState.value = currentPlaybackState.copy(isPlaying = false)
            }
            prepare()
        }
        trackingFacade.track("stop_recording")
    }

    fun redo() {
        cleanup()
        _viewState.value = ViewState.NotRecording
        trackingFacade.track("redo_recording")
    }

    fun play() {
        val currentState = viewState.value
        if (currentState !is ViewState.Playback) {
            throw IllegalStateException("Must be in Playback-state to play")
        }
        if (!currentState.isPrepared) {
            e { "Attempted to play before player was prepared" }
            return
        }
        timer = Timer()
        timer?.schedule(
            timerTask {
                val progress = player?.let { it.currentPosition.toFloat() / it.duration } ?: return@timerTask
                _viewState.update { vs ->
                    if (vs is ViewState.Playback) {
                        vs.copy(progress = progress)
                    } else {
                        vs
                    }
                }
            },
            0,
            1000L / 60,
        )
        _viewState.value = currentState.copy(isPlaying = true)
        player?.start()
        trackingFacade.track("playback_recording")
    }

    fun pause() {
        val currentState = viewState.value as? ViewState.Playback
        if (currentState !is ViewState.Playback) {
            throw IllegalStateException("Must be in Playback-state to pause")
        }
        cleanupTimer()
        player?.pause()
        _viewState.value = currentState.copy(isPlaying = false)
    }

    override fun onCleared() {
        super.onCleared()

        cleanup()
    }

    private fun cleanupTimer() {
        timer?.cancel()
        timer = null
    }

    private fun cleanup() {
        cleanupTimer()

        recorder?.stop()
        recorder?.release()
        recorder = null

        player?.stop()
        player?.release()
        player = null
    }

    companion object {
        private inline fun timerTask(crossinline run: () -> Unit) = object : TimerTask() {
            override fun run() {
                run()
            }
        }
    }
}
