package com.hedvig.feature.claim.chat.data

import com.hedvig.android.core.fileupload.CommonFile
import kotlin.time.Clock

internal class NativeAudioRecordingManager(
  private val clock: Clock,
) : AudioRecordingManager {
  override fun startRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Recording) -> Unit) {
    // TODO: Implement iOS audio recording using AVAudioRecorder
    TODO("iOS audio recording not yet implemented")
  }

  override fun stopRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Playback) -> Unit) {
    // TODO: Implement iOS audio recording stop
    TODO("iOS audio recording stop not yet implemented")
  }

  override fun getRecordedFile(): CommonFile? {
    // TODO: Return the recorded file
    return null
  }

  override fun cleanup() {
    // TODO: Cleanup iOS audio resources
  }

  override fun reset() {
    // TODO: Reset iOS audio recording state
  }
}
