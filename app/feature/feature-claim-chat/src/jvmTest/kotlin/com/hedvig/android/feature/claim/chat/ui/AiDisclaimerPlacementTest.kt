package com.hedvig.android.feature.claim.chat.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PreviewImageLoader
import com.hedvig.android.feature.claim.chat.ClaimChatUiState
import com.hedvig.android.feature.claim.chat.data.ClaimIntentId
import com.hedvig.android.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.data.StepId
import hedvig.resources.CLAIM_CHAT_AI_INFO
import hedvig.resources.Res
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

@OptIn(ExperimentalTestApi::class)
class AiDisclaimerPlacementTest {
  private val disclaimer = runBlocking { getString(Res.string.CLAIM_CHAT_AI_INFO) }

  @Test
  fun `the disclaimer stays until the list has scrolled it away, not the moment the second step lands`() =
    runComposeUiTest {
      mainClock.autoAdvance = false
      var steps by mutableStateOf(listOf(selectStep("first")))
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
      onNodeWithText(disclaimer).assertExists()

      steps = steps + selectStep("second")
      mainClock.advanceTimeByFrame()

      // Dropping it on this frame pulls every step up by its height, ahead of the scroll that follows a frame later.
      onNodeWithText(disclaimer).assertExists()

      mainClock.advanceTimeBy(SETTLE_MILLIS)
      onNodeWithText(disclaimer).assertDoesNotExist()
    }

  private fun selectStep(id: String) = ClaimIntentStep(
    id = StepId(id),
    text = "What does your claim concern?",
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
  }
}
