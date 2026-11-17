package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class EmailInputStateTest {
  private data class PresenterEmail(val email: String, val revision: Int)

  @Test
  fun `typing is kept when the presenter then emits older text under the same revision`() = runComposeUiTest {
    var presenterEmail by mutableStateOf(PresenterEmail("a", 1))
    lateinit var field: MutableState<String>
    setContent {
      field = rememberEmailInput(presenterEmail.email, presenterEmail.revision)
    }
    waitForIdle()
    assertThat(field.value).isEqualTo("a")

    // The member types two characters, and the presenter has only applied the first of them
    runOnIdle { field.value = "ab" }
    runOnIdle { field.value = "abc" }
    presenterEmail = PresenterEmail("ab", 1)
    waitForIdle()
    assertThat(field.value).isEqualTo("abc")

    presenterEmail = PresenterEmail("a", 1)
    waitForIdle()
    assertThat(field.value).isEqualTo("abc")
  }

  @Test
  fun `a prefill with a new revision replaces the typed text`() = runComposeUiTest {
    var presenterEmail by mutableStateOf(PresenterEmail("", 0))
    lateinit var field: MutableState<String>
    setContent {
      field = rememberEmailInput(presenterEmail.email, presenterEmail.revision)
    }
    waitForIdle()
    runOnIdle { field.value = "typed" }
    waitForIdle()

    presenterEmail = PresenterEmail("stored@hedvig.com", 1)
    waitForIdle()
    assertThat(field.value).isEqualTo("stored@hedvig.com")
  }
}
