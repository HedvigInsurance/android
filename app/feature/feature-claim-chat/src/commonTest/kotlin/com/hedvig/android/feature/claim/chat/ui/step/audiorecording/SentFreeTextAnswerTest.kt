package com.hedvig.android.feature.claim.chat.ui.step.audiorecording

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.feature.claim.chat.data.AudioPath
import com.hedvig.android.feature.claim.chat.data.AudioRecordingStepState
import kotlin.test.Test

class SentFreeTextAnswerTest {
  @Test
  fun `a real answer is rendered as the sent answer`() {
    assertThat(freeTextDescription("I dropped my phone").sentFreeTextAnswer()).isEqualTo("I dropped my phone")
  }

  @Test
  fun `surrounding whitespace is kept on an otherwise real answer`() {
    assertThat(freeTextDescription(" I dropped my phone ").sentFreeTextAnswer()).isEqualTo(" I dropped my phone ")
  }

  @Test
  fun `a null answer reads as skipped`() {
    assertThat(freeTextDescription(null).sentFreeTextAnswer()).isNull()
  }

  @Test
  fun `an empty answer reads as skipped rather than an empty bubble`() {
    assertThat(freeTextDescription("").sentFreeTextAnswer()).isNull()
  }

  @Test
  fun `a whitespace only answer reads as skipped rather than an empty bubble`() {
    assertThat(freeTextDescription("   ").sentFreeTextAnswer()).isNull()
  }

  @Test
  fun `a recording state never renders as a sent free text answer`() {
    assertThat(AudioRecordingStepState.AudioRecording.NotRecording.sentFreeTextAnswer()).isNull()
    val playback = AudioRecordingStepState.AudioRecording.Playback(
      audioPath = AudioPath.RemoteUrl("https://example.com/recording.aac"),
      isPlaying = false,
      isPrepared = true,
      hasError = false,
    )
    assertThat(playback.sentFreeTextAnswer()).isNull()
  }

  private fun freeTextDescription(freeText: String?) = AudioRecordingStepState.FreeTextDescription(
    errorType = null,
    canSubmit = true,
    freeText = freeText,
  )
}
