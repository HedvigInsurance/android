package com.hedvig.feature.claim.chat.data

import com.hedvig.android.core.fileupload.CommonFile

interface AudioRecordingManager {
  fun startRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Recording) -> Unit)

  fun stopRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Playback) -> Unit)

  fun getRecordedFile(): CommonFile?

  fun cleanup()

  fun reset()
}
