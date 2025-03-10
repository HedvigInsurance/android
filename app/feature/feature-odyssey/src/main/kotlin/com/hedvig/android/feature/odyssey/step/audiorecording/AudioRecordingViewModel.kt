package com.hedvig.android.feature.odyssey.step.audiorecording

import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimflow.AudioContent
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.model.AudioUrl
import java.io.File
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class AudioRecordingViewModel(
  private val audioRecording: ClaimFlowDestination.AudioRecording,
  private val claimFlowRepository: ClaimFlowRepository,
  val clock: Clock = Clock.System,
) : ViewModel() {
  private var recorder: MediaRecorder? = null
  private var timer: Timer? = null
  private var player: MediaPlayer? = null
  private val incomingAudioContent = audioRecording.audioContent

  private val _uiState: MutableStateFlow<AudioRecordingUiState> = MutableStateFlow(
    if (incomingAudioContent != null) {
      AudioRecordingUiState.PrerecordedWithAudioContent(incomingAudioContent)
    } else {
      AudioRecordingUiState.NotRecording
    },
  )
  val uiState = _uiState.asStateFlow()

  fun submitAudioFile(audioFile: File) {
    val uiState = _uiState.value as? AudioRecordingUiState.Playback ?: return
    if (uiState.hasError || uiState.isLoading) return
    _uiState.update { uiState.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.submitAudioRecording(audioRecording.flowId, audioFile).fold(
        ifLeft = {
          _uiState.update {
            uiState.copy(isLoading = false, hasError = true)
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

  fun submitAudioUrl(audioUrl: AudioUrl) {
    val uiState = _uiState.value as? AudioRecordingUiState.PrerecordedWithAudioContent ?: return
    if (uiState.hasError || uiState.isLoading) return
    _uiState.update { uiState.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.submitAudioUrl(audioRecording.flowId, audioUrl).fold(
        ifLeft = {
          _uiState.update {
            uiState.copy(isLoading = false, hasError = true)
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
    _uiState.update { oldUiState ->
      when (oldUiState) {
        is AudioRecordingUiState.Playback -> oldUiState.copy(hasError = false)
        is AudioRecordingUiState.PrerecordedWithAudioContent -> oldUiState.copy(hasError = false)
        else -> oldUiState
      }
    }
  }

  fun handledNextStepNavigation() {
    val uiState = _uiState.value
    if (uiState is AudioRecordingUiState.Playback) {
      _uiState.update { uiState.copy(nextStep = null) }
    } else if (uiState is AudioRecordingUiState.PrerecordedWithAudioContent) {
      _uiState.update { uiState.copy(nextStep = null) }
    }
  }

  fun startRecording() {
    if (_uiState.value.isLoading) return
    if (recorder == null) {
      recorder = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioSamplingRate(96_000)
        setAudioEncodingBitRate(128_000)
        val filePath = File.createTempFile(
          "claim_android_recording_${UUID.randomUUID()}",
          ".mp4",
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
    if (_uiState.value.isLoading) return
    cleanup()
    _uiState.value = AudioRecordingUiState.NotRecording
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

internal sealed interface AudioRecordingUiState {
  val canSubmit: Boolean
    get() {
      val playbackCanSubmit = this is Playback && !isPlaying && nextStep == null && !isLoading && !hasError
      val prerecordedCanSubmit = this is PrerecordedWithAudioContent && nextStep == null && !isLoading && !hasError
      return playbackCanSubmit || prerecordedCanSubmit
    }

  val nextStep: ClaimFlowStep?
    get() = null

  val isLoading: Boolean
    get() = false

  val hasError: Boolean
    get() = false

  object NotRecording : AudioRecordingUiState

  data class Recording(
    val amplitudes: List<Int>,
    val startedAt: Instant,
    val filePath: String,
  ) : AudioRecordingUiState

  data class PrerecordedWithAudioContent(
    val audioContent: AudioContent,
    override val nextStep: ClaimFlowStep? = null,
    override val isLoading: Boolean = false,
    override val hasError: Boolean = false,
  ) : AudioRecordingUiState

  data class Playback(
    val filePath: String,
    val isPlaying: Boolean,
    val isPrepared: Boolean,
    val amplitudes: List<Int>,
    override val nextStep: ClaimFlowStep?,
    override val isLoading: Boolean,
    override val hasError: Boolean,
  ) : AudioRecordingUiState
}
