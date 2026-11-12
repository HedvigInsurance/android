package com.hedvig.android.feature.claim.chat.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.fileupload.CommonFile
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * There is no audio recorder here, so [startRecording] reports the recorder's error state straight away, which
 * leaves answering by text as the way forward. No recording is ever made, so the remaining members have nothing to
 * act on.
 */
@Inject
@ContributesBinding(AppScope::class)
internal class JvmAudioRecordingManager : AudioRecordingManager {
  override fun startRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording) -> Unit) {
    onStateUpdate(
      AudioRecordingStepState.AudioRecording.Playback(
        audioPath = AudioPath.FilePath(""),
        isPlaying = false,
        isPrepared = false,
        hasError = true,
      ),
    )
  }

  override fun stopRecording(onStateUpdate: (AudioRecordingStepState.AudioRecording.Playback) -> Unit) {}

  override fun getRecordedFile(): CommonFile? = null

  override fun cleanup() {}

  override fun reset() {}
}
