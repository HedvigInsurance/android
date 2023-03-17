package com.hedvig.android.odyssey.step.audiorecording

import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.model.FlowId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.io.File
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

internal class AudioRecordingViewModel(
  private val flowId: FlowId,
  private val claimFlowRepository: ClaimFlowRepository,
  val clock: Clock = Clock.System,
) : ViewModel() {

  private var recorder: MediaRecorder? = null
  private var timer: Timer? = null
  private var player: MediaPlayer? = null

  private val _uiState = MutableStateFlow<AudioRecordingUiState>(AudioRecordingUiState.NotRecording)
  val uiState = _uiState.asStateFlow()

  fun submitAudioFile(audioFile: File) {
    val uiState = _uiState.value as? AudioRecordingUiState.Playback ?: return
    if (uiState.hasError || uiState.isLoading) return
    viewModelScope.launch {
      claimFlowRepository.submitAudioRecording(flowId, audioFile).fold(
        ifLeft = {
          _uiState.update {
            uiState.copy(hasError = true)
          }
        },
        ifRight = { claimFlowStep ->
          _uiState.update {
            uiState.copy(isLoading = false, nextStep = claimFlowStep)
          }
        },
      )
    }
  }

  fun showedError() {
    _uiState.update {
      if (it is AudioRecordingUiState.Playback) {
        it.copy(hasError = false)
      } else {
        it
      }
    }
  }

  fun handledNextStepNavigation() {
    val uiState = _uiState.value
    if (uiState is AudioRecordingUiState.Playback) {
      _uiState.update { uiState.copy(nextStep = null) }
    }
  }

  fun startRecording() {
    if ((_uiState.value as? AudioRecordingUiState.Playback)?.isLoading == true) return
    if (recorder == null) {
      recorder = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
        setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        setAudioSamplingRate(96_000)
        setAudioEncodingBitRate(128_000)
        val filePath = File.createTempFile(
          "claim_android_recording_${UUID.randomUUID()}",
          ".aac",
        ).absolutePath
        setOutputFile(filePath)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        prepare()
        start()
        _uiState.value = AudioRecordingUiState.Recording(emptyList(), clock.now(), filePath)
      }
      timer = Timer()
      timer?.schedule(
        timerTask {
          recorder?.maxAmplitude?.let { amplitude ->
            _uiState.update { vs ->
              if (vs is AudioRecordingUiState.Recording) {
                vs.copy(amplitudes = vs.amplitudes + amplitude)
              } else {
                vs
              }
            }
          }
        },
        0L,
        1000L / 60,
      )
    }
  }

  fun stopRecording() {
    val currentState = uiState.value
    if (currentState !is AudioRecordingUiState.Recording) {
      throw IllegalStateException("Must be in Recording-state to stop recording")
    }
    cleanup()
    player = MediaPlayer().apply {
      setDataSource(currentState.filePath)
      setOnPreparedListener {
        _uiState.value = AudioRecordingUiState.Playback(
          filePath = currentState.filePath,
          isPlaying = false,
          isPrepared = true,
          amplitudes = currentState.amplitudes,
          progress = 0f,
          nextStep = null,
          isLoading = false,
          hasError = false,
        )
      }
      setOnCompletionListener {
        // Bail if the user has backed out of the playback-state
        val currentPlaybackState = uiState.value as? AudioRecordingUiState.Playback ?: return@setOnCompletionListener
        cleanupTimer()
        _uiState.value = currentPlaybackState.copy(isPlaying = false)
      }
      prepare()
    }
  }

  fun redo() {
    if ((_uiState.value as? AudioRecordingUiState.Playback)?.isLoading == true) return
    cleanup()
    _uiState.value = AudioRecordingUiState.NotRecording
  }

  fun play() {
    val currentState = uiState.value
    if (currentState !is AudioRecordingUiState.Playback) {
      throw IllegalStateException("Must be in Playback-state to play")
    }
    if (!currentState.isPrepared) {
      return
    }
    timer = Timer()
    timer?.schedule(
      timerTask {
        val progress = player?.let { it.currentPosition.toFloat() / it.duration } ?: return@timerTask
        _uiState.update { vs ->
          if (vs is AudioRecordingUiState.Playback) {
            vs.copy(progress = progress)
          } else {
            vs
          }
        }
      },
      0,
      1000L / 60,
    )
    _uiState.value = currentState.copy(isPlaying = true)
    player?.start()
  }

  fun pause() {
    val currentState = uiState.value as? AudioRecordingUiState.Playback
    if (currentState?.isLoading == true) return
    if (currentState !is AudioRecordingUiState.Playback) {
      throw IllegalStateException("Must be in Playback-state to pause")
    }
    cleanupTimer()
    player?.pause()
    _uiState.value = currentState.copy(isPlaying = false)
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

internal sealed class AudioRecordingUiState {
  val hasAudioSubmissionError: Boolean
    get() = this is Playback && hasError

  val canSubmit: Boolean // todo use this to disable interactions on the buttons
    get() = this is Playback && !this.isPlaying && this.nextStep == null && !isLoading && !hasError

  object NotRecording : AudioRecordingUiState()
  data class Recording(
    val amplitudes: List<Int>,
    val startedAt: Instant,
    val filePath: String,
  ) : AudioRecordingUiState()

  data class Playback(
    val filePath: String,
    val isPlaying: Boolean,
    val isPrepared: Boolean,
    val amplitudes: List<Int>,
    val progress: Float,
    val nextStep: ClaimFlowStep?,
    val isLoading: Boolean,
    val hasError: Boolean,
  ) : AudioRecordingUiState()
}
