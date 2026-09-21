package com.hedvig.android.feature.claim.chat.ui.step.audiorecording

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class FreeTextDraftTest {
  @Test
  fun `a draft opened on an answer starts from it, with the caret after it`() {
    val state = FreeTextDraftState.seededFrom("I dropped my phone")

    assertThat(state.value.text).isEqualTo("I dropped my phone")
    assertThat(state.value.selection).isEqualTo(TextRange(18))
  }

  @Test
  fun `a draft opened on a step with nothing answered yet starts empty`() {
    val state = FreeTextDraftState.seededFrom("")

    assertThat(state.value).isEqualTo(TextFieldValue("", TextRange(0)))
  }

  @Test
  fun `an answer part way through is saved and restored whole`() {
    // What carries the answer across a configuration change, which is the rotation that disposes whichever
    // card was drawing it. The selection travels with the text, so the caret comes back where it was.
    val state = FreeTextDraftState()
    state.value = TextFieldValue("I dropped my phone", TextRange(2, 9))

    val saved = with(FreeTextDraftState.Saver) { saverScope.save(state) }
    val restored = FreeTextDraftState.Saver.restore(saved!!)!!

    assertThat(restored.value).isEqualTo(state.value)
  }

  private val saverScope = SaverScope { true }
}
