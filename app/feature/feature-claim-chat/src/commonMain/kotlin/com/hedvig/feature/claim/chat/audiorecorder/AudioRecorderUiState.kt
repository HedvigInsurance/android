package com.hedvig.feature.claim.chat.audiorecorder

import kotlin.time.Instant

sealed interface AudioRecorderUiState {
  val canSubmit: Boolean
    get() {
      val playbackCanSubmit =
        this is AudioRecording.Playback && !isPlaying && hasNextStep && !isLoading && !hasError
      val prerecordedCanSubmit =
        this is AudioRecording.PrerecordedWithAudioContent && hasNextStep && !isLoading && !hasError
      val freeTextCanSubmit =
        this is FreeTextDescription && hasNextStep && !isLoading && !hasError && !this.freeText.isNullOrEmpty()
      return playbackCanSubmit || prerecordedCanSubmit || freeTextCanSubmit
    }

  val hasNextStep: Boolean
    get() = false

  val isLoading: Boolean
    get() = false

  val hasError: Boolean
    get() = false

  data class FreeTextDescription(
    val freeText: String?,
    val showOverlay: Boolean,
    val errorType: FreeTextErrorType?,
    override val hasNextStep: Boolean = false,
    override val isLoading: Boolean = false,
    override val hasError: Boolean = false,
  ) : AudioRecorderUiState

  sealed interface AudioRecording : AudioRecorderUiState {
    data object NotRecording : AudioRecording

    data class Recording(
      val amplitudes: List<Int>,
      val startedAt: Instant,
      val filePath: String,
    ) : AudioRecording

    data class PrerecordedWithAudioContent(
      val audioContent: AudioContent,
      override val hasNextStep: Boolean = false,
      override val isLoading: Boolean = false,
      override val hasError: Boolean = false,
    ) : AudioRecording

    data class Playback(
      val filePath: String,
      val isPlaying: Boolean,
      val isPrepared: Boolean,
      val amplitudes: List<Int>,
      override val hasNextStep: Boolean,
      override val isLoading: Boolean,
      override val hasError: Boolean,
    ) : AudioRecording
  }

  enum class ScreenMode {
    RECORDING,
    FREE_TEXT,
  }

  sealed interface FreeTextErrorType {
    data class TooShort(val minLength: Int) : FreeTextErrorType
  }
}
