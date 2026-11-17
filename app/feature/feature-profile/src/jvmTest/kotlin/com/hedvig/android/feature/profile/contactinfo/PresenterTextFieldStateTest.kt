package com.hedvig.android.feature.profile.contactinfo

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class PresenterTextFieldStateTest {
  private data class PresenterText(val text: String, val revision: Int)

  @Test
  fun `typing is kept when the presenter then emits older text under the same revision`() = runComposeUiTest {
    var presenterText by mutableStateOf(PresenterText("a", 1))
    val forwarded = mutableListOf<String>()
    lateinit var field: TextFieldState
    setContent {
      field = rememberPresenterTextFieldState(presenterText.text, presenterText.revision) { forwarded += it }
    }
    waitForIdle()
    assertThat(field.text.toString()).isEqualTo("a")

    // The member types two characters, and the presenter has only applied the first of them
    runOnIdle { field.typeAtEnd("b") }
    runOnIdle { field.typeAtEnd("c") }
    presenterText = PresenterText("ab", 1)
    waitForIdle()
    assertThat(field.text.toString()).isEqualTo("abc")
    assertThat(forwarded.last()).isEqualTo("abc")

    // Older still, as a presenter that has applied none of the typing would emit
    presenterText = PresenterText("a", 1)
    waitForIdle()
    assertThat(field.text.toString()).isEqualTo("abc")
  }

  @Test
  fun `a new revision copies the presenter text into the field and the copy is forwarded back`() = runComposeUiTest {
    var presenterText by mutableStateOf(PresenterText("", 0))
    val forwarded = mutableListOf<String>()
    lateinit var field: TextFieldState
    setContent {
      field = rememberPresenterTextFieldState(presenterText.text, presenterText.revision) { forwarded += it }
    }
    waitForIdle()
    runOnIdle { field.typeAtEnd("typed") }
    waitForIdle()

    presenterText = PresenterText("stored@hedvig.com", 1)
    waitForIdle()
    assertThat(field.text.toString()).isEqualTo("stored@hedvig.com")
    assertThat(field.selection.start).isEqualTo("stored@hedvig.com".length)
    assertThat(forwarded).containsExactly("", "typed", "stored@hedvig.com")
  }

  @Test
  fun `the first text the field shows is the presenter text, without waiting for an effect`() = runComposeUiTest {
    lateinit var field: TextFieldState
    var textOnFirstComposition: String? = null
    setContent {
      field = rememberPresenterTextFieldState("prefilled@hedvig.com", 1) {}
      if (textOnFirstComposition == null) textOnFirstComposition = field.text.toString()
    }
    waitForIdle()
    assertThat(textOnFirstComposition).isEqualTo("prefilled@hedvig.com")
  }
}

private fun TextFieldState.typeAtEnd(text: String) {
  edit {
    append(text)
    placeCursorAtEnd()
  }
}
