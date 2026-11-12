package com.hedvig.android.feature.claim.chat.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph
import kotlin.test.Test

@DependencyGraph(AppScope::class)
internal interface AudioRecordingManagerTestGraph {
  val audioRecordingManager: AudioRecordingManager
}

class JvmAudioRecordingManagerTest {
  @Test
  fun `the app scope binds AudioRecordingManager to the JVM implementation`() {
    val graph = createGraph<AudioRecordingManagerTestGraph>()

    assertThat(graph.audioRecordingManager).isInstanceOf<JvmAudioRecordingManager>()
  }

  @Test
  fun `starting a recording reports the recorder's error state`() {
    val states = mutableListOf<AudioRecordingStepState.AudioRecording>()

    JvmAudioRecordingManager().startRecording { states.add(it) }

    assertThat(states).containsExactly(
      AudioRecordingStepState.AudioRecording.Playback(
        audioPath = AudioPath.FilePath(""),
        isPlaying = false,
        isPrepared = false,
        hasError = true,
      ),
    )
  }

  @Test
  fun `there is never a recorded file`() {
    val manager = JvmAudioRecordingManager()
    manager.startRecording {}

    assertThat(manager.getRecordedFile()).isNull()
  }
}
