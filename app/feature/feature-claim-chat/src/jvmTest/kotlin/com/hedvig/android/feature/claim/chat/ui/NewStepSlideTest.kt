package com.hedvig.android.feature.claim.chat.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isGreaterThan
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PreviewImageLoader
import com.hedvig.android.feature.claim.chat.ClaimChatUiState
import com.hedvig.android.feature.claim.chat.data.ClaimIntentId
import com.hedvig.android.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.data.StepId
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class NewStepSlideTest {
  @Test
  fun `a new step slides up to the bottom of the list instead of cutting to it`() = runComposeUiTest {
    mainClock.autoAdvance = false
    var steps by mutableStateOf(listOf(selectStep("first"), selectStep("second")))
    setContent {
      HedvigTheme(darkTheme = false) {
        ClaimChatScreen(
          uiState = claimChat(steps),
          onEvent = {},
          shouldShowRequestPermissionRationale = { false },
          onNavigateToImageViewer = { _, _ -> },
          navigateToDeflect = { _, _ -> },
          appPackageId = "test",
          imageLoader = PreviewImageLoader(),
          navigateUp = {},
          navigateBack = {},
          openAppSettings = {},
          openPlayStore = {},
        )
      }
    }
    mainClock.advanceTimeBy(SETTLE_MILLIS)

    steps = steps + selectStep("third")
    mainClock.advanceTimeByFrame()
    val landed = topOfQuestion("third")
    mainClock.advanceTimeBy(SLIDE_MIDPOINT_MILLIS)
    val midway = topOfQuestion("third")
    mainClock.advanceTimeBy(SETTLE_MILLIS)
    val settled = topOfQuestion("third")

    assertThat(landed).isGreaterThan(midway)
    assertThat(midway).isGreaterThan(settled)
  }

  private fun ComposeUiTest.topOfQuestion(id: String) = onNodeWithText(question(id)).getUnclippedBoundsInRoot().top

  private fun question(id: String) = "Question for the $id step"

  private fun selectStep(id: String) = ClaimIntentStep(
    id = StepId(id),
    text = question(id),
    stepContent = StepContent.ContentSelect(
      options = listOf(StepContent.ContentSelect.Option(id = "things", title = "My things")),
      selectedOptionId = null,
      style = StepContent.ContentSelectStyle.PILL,
      isSkippable = false,
    ),
    isRegrettable = true,
    hint = null,
    showSpinForThisStep = false,
  )

  private fun claimChat(steps: List<ClaimIntentStep>) = ClaimChatUiState.ClaimChat(
    claimIntentId = ClaimIntentId("claim"),
    steps = steps,
    currentStep = steps.last(),
    outcome = null,
    errorSubmittingStep = null,
    canRetryFailedSubmission = false,
    showConfirmEditDialogForStep = null,
    stepsWithShownAnimations = steps.map { it.id },
    progress = null,
    searchQuery = null,
    title = null,
    isResumable = false,
    resumeClaimEnabled = false,
  )

  private companion object {
    const val SETTLE_MILLIS = 5_000L
    const val SLIDE_MIDPOINT_MILLIS = 150L
  }
}
