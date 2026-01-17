package com.hedvig.feature.claim.chat.data

import com.hedvig.feature.claim.chat.data.file.CommonFile
import kotlin.time.Clock

interface AudioRecordingManager {
  fun startRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Recording) -> Unit)

  fun stopRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Playback) -> Unit)

  fun getRecordedFile(): CommonFile?

  fun cleanup()

  fun reset()

  companion object {
    const val MIN_TEXT_LENGTH = 50
  }
}
