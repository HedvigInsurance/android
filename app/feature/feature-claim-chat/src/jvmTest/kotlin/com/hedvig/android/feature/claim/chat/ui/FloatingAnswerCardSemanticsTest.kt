package com.hedvig.android.feature.claim.chat.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PreviewImageLoader
import com.hedvig.android.feature.claim.chat.ClaimChatEvent
import com.hedvig.android.feature.claim.chat.ClaimChatUiState
import com.hedvig.android.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.android.feature.claim.chat.data.ClaimIntentId
import com.hedvig.android.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.data.StepId
import hedvig.resources.Res
import hedvig.resources.claims_record
import hedvig.resources.claims_skip_button
import hedvig.resources.claims_write
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

/**
 * What a screen reader would meet on the voice and text step, checked through the semantics tree rather than with
 * TalkBack itself. Order is the tree's depth-first order, which is the order TalkBack walks for these nodes.
 */
@OptIn(ExperimentalTestApi::class)
class FloatingAnswerCardSemanticsTest {
  private val write = runBlocking { getString(Res.string.claims_write) }
  private val record = runBlocking { getString(Res.string.claims_record) }
  private val skip = runBlocking { getString(Res.string.claims_skip_button) }

  @Test
  fun `the question is read before the answer card's actions`() = runComposeUiTest {
    showClaimChat(voiceStep(AudioRecordingStepState.AudioRecording.NotRecording), revealed = true)

    val order = textsInTraversalOrder()
    val question = order.indexOf(QUESTION)
    assertThat(order.indexOf(write)).isGreaterThan(question)
    assertThat(order.indexOf(record)).isGreaterThan(order.indexOf(write))
    assertThat(order.indexOf(skip)).isGreaterThan(order.indexOf(record))
  }

  @Test
  fun `the card's actions are not in the tree while it is hidden`() = runComposeUiTest {
    mainClock.autoAdvance = false
    showClaimChat(voiceStep(AudioRecordingStepState.AudioRecording.NotRecording), revealed = false)
    mainClock.advanceTimeByFrame()

    // Held back until the question has revealed.
    assertThat(textsInTraversalOrder().count { it == write || it == record || it == skip }).isEqualTo(0)

    mainClock.advanceTimeBy(REVEAL_AND_SLIDE_MILLIS)
    assertThat(textsInTraversalOrder().count { it == write || it == record || it == skip }).isEqualTo(3)
  }

  @Test
  fun `Write focuses the answer field straight away`() = runComposeUiTest {
    val step = voiceStep(AudioRecordingStepState.AudioRecording.NotRecording)
    var uiState by mutableStateOf(claimChat(step, revealed = true))
    showClaimChat(
      state = { uiState },
      onEvent = { event ->
        if (event is ClaimChatEvent.AudioRecording.SwitchToFreeText) {
          uiState = claimChat(voiceStep(FREE_TEXT_EMPTY), revealed = true)
        }
      },
    )

    onNodeWithText(write).performClick()
    waitForIdle()

    onNode(hasSetTextAction()).assertIsFocused()
  }

  @Test
  fun `a step arriving in text mode takes focus only once its card has slid in`() = runComposeUiTest {
    mainClock.autoAdvance = false
    showClaimChat(voiceStep(FREE_TEXT_EMPTY), revealed = false)
    mainClock.advanceTimeByFrame()

    onNode(hasSetTextAction(), useUnmergedTree = true).assertIsNotFocused()

    mainClock.advanceTimeBy(REVEAL_AND_SLIDE_MILLIS)
    onNode(hasSetTextAction(), useUnmergedTree = true).assertIsFocused()
  }

  private fun ComposeUiTest.showClaimChat(step: ClaimIntentStep, revealed: Boolean) {
    val uiState = claimChat(step, revealed)
    showClaimChat(state = { uiState }, onEvent = {})
  }

  private fun ComposeUiTest.showClaimChat(state: () -> ClaimChatUiState.ClaimChat, onEvent: (ClaimChatEvent) -> Unit) {
    setContent {
      HedvigTheme(darkTheme = false) {
        ClaimChatScreen(
          uiState = state(),
          onEvent = onEvent,
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

  private fun ComposeUiTest.textsInTraversalOrder(): List<String> {
    fun SemanticsNode.walk(): List<String> {
      val own = config.getOrNull(SemanticsProperties.Text)?.map { it.text }.orEmpty()
      return own + children.flatMap { it.walk() }
    }
    // The merged tree, which is what TalkBack reads; a cleared node's descendants still show in the unmerged one.
    return onRoot().fetchSemanticsNode().walk()
  }

  private fun voiceStep(recordingState: AudioRecordingStepState) = ClaimIntentStep(
    id = StepId("voice"),
    text = QUESTION,
    stepContent = StepContent.AudioRecording(
      uploadUri = "https://example.com/upload",
      isSkippable = true,
      recordingState = recordingState,
      freeTextMinLength = 0,
      freeTextMaxLength = 2000,
    ),
    isRegrettable = true,
    hint = null,
    showSpinForThisStep = false,
  )

  private fun claimChat(step: ClaimIntentStep, revealed: Boolean) = ClaimChatUiState.ClaimChat(
    claimIntentId = ClaimIntentId("claim"),
    steps = listOf(step),
    currentStep = step,
    outcome = null,
    errorSubmittingStep = null,
    canRetryFailedSubmission = false,
    showConfirmEditDialogForStep = null,
    stepsWithShownAnimations = if (revealed) listOf(step.id) else emptyList(),
    progress = null,
    searchQuery = null,
    title = null,
    isResumable = false,
    resumeClaimEnabled = false,
  )

  private companion object {
    const val QUESTION = "What happened?"
    val FREE_TEXT_EMPTY = AudioRecordingStepState.FreeTextDescription(errorType = null, canSubmit = true)

    // Long enough for the question's text reveal, the answer's fade in and the card's slide.
    const val REVEAL_AND_SLIDE_MILLIS = 10_000L
  }
}
