package com.hedvig.app.feature.embark.passages.audiorecorder

import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.Clock
import java.time.Instant
import java.util.Timer
import java.util.TimerTask

class AudioRecorderViewModel(
    private val clock: Clock,
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
                val filePath = File.createTempFile("test_claim_file", null).absolutePath
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
        }
    }

    fun stopRecording() {
        val currentState = viewState.value
        if (currentState !is ViewState.Recording) {
            throw IllegalStateException("Must be in Recording-state to stop recording")
        }
        cleanup()
        _viewState.value = ViewState.Playback(
            currentState.filePath,
            false,
        )
    }

    fun redo() {
        cleanup()
        _viewState.value = ViewState.NotRecording
    }

    fun play() {
        val currentState = viewState.value
        if (currentState !is ViewState.Playback) {
            throw IllegalStateException("Must be in Playback-state to play")
        }
        viewModelScope.launch {
            player = MediaPlayer().apply {
                setDataSource(currentState.filePath)
                setOnPreparedListener {
                    _viewState.value = currentState.copy(isPlaying = true) // TODO: Copy current state instead
                    start()
                }
                setOnCompletionListener {
                    _viewState.value = currentState.copy(isPlaying = false)
                }
                prepareAsync()
            }
        }
    }

    fun pause() {
        player?.pause()
    }

    override fun onCleared() {
        super.onCleared()

        cleanup()
    }

    private fun cleanup() {
        timer?.cancel()
        timer = null

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
