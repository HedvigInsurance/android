package com.hedvig.android.feature.claim.chat.ui.step

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.data.StepId
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ContentSelectStepTest {
  @Test
  fun `the options are gone on the frame the step stops being the current one`() = runComposeUiTest {
    mainClock.autoAdvance = false
    var isCurrentStep by mutableStateOf(true)
    setContent {
      HedvigTheme(darkTheme = false) {
        ContentSelectStep(
          stepContent = StepContent.ContentSelect(
            options = OPTIONS,
            selectedOptionId = null,
            style = StepContent.ContentSelectStyle.PILL,
            isSkippable = false,
          ),
          itemId = StepId("select"),
          isRegrettable = true,
          isCurrentStep = isCurrentStep,
          options = OPTIONS,
          selectedOptionId = SELECTED.id,
          onEvent = {},
          currentContinueButtonLoading = false,
          canSkip = false,
          onSkip = {},
          skipButtonLoading = false,
        )
      }
    }
    mainClock.advanceTimeByFrame()
    onNodeWithText(OTHER.title).assertExists()

    isCurrentStep = false
    mainClock.advanceTimeByFrame()

    // The step loses its minimum height on this same frame, so an option still on screen would ride up the list
    // from the bottom of the viewport to the top of the step for as long as it lingered.
    onNodeWithText(OTHER.title).assertDoesNotExist()
    onNodeWithText(SELECTED.title, useUnmergedTree = true).assertExists()
  }

  private companion object {
    val SELECTED = StepContent.ContentSelect.Option(id = "things", title = "My things")
    val OTHER = StepContent.ContentSelect.Option(id = "dog", title = "My dog")
    val OPTIONS = listOf(SELECTED, OTHER)
  }
}
