package com.hedvig.android.feature.claim.chat.ui.step.audiorecording

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isGreaterThanOrEqualTo
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PreviewImageLoader
import com.hedvig.android.feature.claim.chat.ClaimChatUiState
import com.hedvig.android.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.android.feature.claim.chat.data.ClaimIntentId
import com.hedvig.android.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.data.StepId
import com.hedvig.android.feature.claim.chat.ui.ClaimChatScreen
import hedvig.resources.Res
import hedvig.resources.claims_record
import hedvig.resources.claims_write
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

@OptIn(ExperimentalTestApi::class)
class WaysToAnswerLayoutTest {
  private val write = runBlocking { getString(Res.string.claims_write) }
  private val record = runBlocking { getString(Res.string.claims_record) }

  @Test
  fun `Write and Record sit side by side when both fit on one line`() = runComposeUiTest {
    showWaysToAnswer(width = 411.dp, fontScale = 1f)

    val writeBounds = boundsOf(write)
    val recordBounds = boundsOf(record)
    assertThat(recordBounds.top).isEqualTo(writeBounds.top)
    assertThat(recordBounds.left).isGreaterThan(writeBounds.right)
  }

  @Test
  fun `Write and Record stack at full width when a label would not fit beside the other`() = runComposeUiTest {
    showWaysToAnswer(width = 320.dp, fontScale = 1.5f)

    val writeBounds = boundsOf(write)
    val recordBounds = boundsOf(record)
    assertThat(recordBounds.top).isGreaterThanOrEqualTo(writeBounds.bottom)
    assertThat(recordBounds.left).isEqualTo(writeBounds.left)
    assertThat(recordBounds.width).isEqualTo(writeBounds.width)
    assertThat(writeBounds.width).isGreaterThan(320.dp / 2)
  }

  private fun ComposeUiTest.boundsOf(text: String): DpRect = onNodeWithText(text).getUnclippedBoundsInRoot()

  private fun ComposeUiTest.showWaysToAnswer(width: Dp, fontScale: Float) {
    val step = ClaimIntentStep(
      id = StepId("voice"),
      text = "What happened?",
      stepContent = StepContent.AudioRecording(
        uploadUri = "https://example.com/upload",
        isSkippable = true,
        recordingState = AudioRecordingStepState.AudioRecording.NotRecording,
        freeTextMinLength = 0,
        freeTextMaxLength = 2000,
      ),
      isRegrettable = true,
      hint = null,
      showSpinForThisStep = false,
    )
    val uiState = ClaimChatUiState.ClaimChat(
      claimIntentId = ClaimIntentId("claim"),
      steps = listOf(step),
      currentStep = step,
      outcome = null,
      errorSubmittingStep = null,
      canRetryFailedSubmission = false,
      showConfirmEditDialogForStep = null,
      stepsWithShownAnimations = listOf(step.id),
      progress = null,
      searchQuery = null,
      title = null,
      isResumable = false,
      resumeClaimEnabled = false,
    )
    setContent {
      val density = LocalDensity.current
      CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
        HedvigTheme(darkTheme = false) {
          Box(Modifier.requiredWidth(width).fillMaxHeight()) {
            ClaimChatScreen(
              uiState = uiState,
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
      }
    }
    waitForIdle()
  }
}
