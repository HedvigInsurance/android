package com.hedvig.android.feature.claim.chat.ui.step.audiorecording

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

class FreeTextDraftTest {
  @Test
  fun `a session that is already seeded keeps what has been typed`() {
    val seed = freeTextDraftSeed(
      activeSessionId = "step-1",
      seededSessionId = "step-1",
      storedAnswer = "",
    )
    assertThat(seed).isInstanceOf(FreeTextDraftSeed.Keep::class)
  }

  @Test
  fun `a new session starts from the answer the step already holds`() {
    val seed = freeTextDraftSeed(
      activeSessionId = "step-1",
      seededSessionId = null,
      storedAnswer = "I dropped my phone",
    )
    assertThat(seed).isEqualTo(FreeTextDraftSeed.Replace("I dropped my phone"))
  }

  @Test
  fun `the next step does not open on the previous step's answer`() {
    val seed = freeTextDraftSeed(
      activeSessionId = "step-2",
      seededSessionId = "step-1",
      storedAnswer = "",
    )
    assertThat(seed).isEqualTo(FreeTextDraftSeed.Replace(""))
  }

  @Test
  fun `leaving the editor drops the draft`() {
    val seed = freeTextDraftSeed(
      activeSessionId = null,
      seededSessionId = "step-1",
      storedAnswer = "I dropped my phone",
    )
    assertThat(seed).isEqualTo(FreeTextDraftSeed.Replace(""))
  }

  @Test
  fun `an answer part way through survives the card drawing it being swapped for the other`() {
    val state = FreeTextDraftState()
    state.syncToSession("step-1", "")
    state.value = TextFieldValue("I dropped my ph", TextRange(15))

    // What a rotation does: the same session, asked again with the answer the step last saved, which is
    // still nothing because saving only happens on send.
    state.syncToSession("step-1", "")

    assertThat(state.value.text).isEqualTo("I dropped my ph")
    assertThat(state.value.selection).isEqualTo(TextRange(15))
  }

  @Test
  fun `a seeded answer puts the caret after it rather than in front of it`() {
    val state = FreeTextDraftState()
    state.syncToSession("step-1", "I dropped my phone")
    assertThat(state.value.selection).isEqualTo(TextRange(18))
  }

  @Test
  fun `the draft is saved and restored whole`() {
    val state = FreeTextDraftState()
    state.syncToSession("step-1", "")
    state.value = TextFieldValue("I dropped my phone", TextRange(2, 9))

    val saved = with(FreeTextDraftState.Saver) { saverScope.save(state) }
    val restored = FreeTextDraftState.Saver.restore(saved!!)!!

    assertThat(restored.value).isEqualTo(state.value)
    assertThat(restored.sessionId).isEqualTo("step-1")
  }

  private val saverScope = SaverScope { true }
}
