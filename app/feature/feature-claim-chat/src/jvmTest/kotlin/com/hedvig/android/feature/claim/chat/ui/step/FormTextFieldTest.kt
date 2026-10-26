package com.hedvig.android.feature.claim.chat.ui.step

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PreviewImageLoader
import com.hedvig.android.feature.claim.chat.ClaimChatUiState
import com.hedvig.android.feature.claim.chat.data.ClaimIntentId
import com.hedvig.android.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.android.feature.claim.chat.data.FieldId
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.data.StepContent.Form.FieldOption
import com.hedvig.android.feature.claim.chat.data.StepContent.Form.FieldType
import com.hedvig.android.feature.claim.chat.data.StepId
import com.hedvig.android.feature.claim.chat.ui.ClaimChatScreen
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class FormTextFieldTest {
  @Test
  fun `a text field grows to show a long answer on several lines`() = runComposeUiTest {
    showForm(
      field("short", FieldType.TEXT, answer = "Ja"),
      field("long", FieldType.TEXT, answer = LONG_ANSWER),
    )

    val (short, long) = textFieldHeights()
    assertThat(long).isGreaterThan(short)
  }

  @Test
  fun `a number field stays on one line`() = runComposeUiTest {
    showForm(
      field("short", FieldType.NUMBER, answer = "1"),
      field("long", FieldType.NUMBER, answer = "1234567890".repeat(6)),
    )

    val (short, long) = textFieldHeights()
    assertThat(long).isEqualTo(short)
  }

  private fun ComposeUiTest.textFieldHeights(): List<Dp> =
    onAllNodes(hasSetTextAction()).fetchSemanticsNodes().indices.map { index ->
      onAllNodes(hasSetTextAction())[index].getUnclippedBoundsInRoot().height
    }

  private fun field(id: String, type: FieldType, answer: String) = StepContent.Form.Field(
    id = FieldId(id),
    isRequired = false,
    suffix = null,
    title = "Berätta mer",
    defaultValues = emptyList(),
    maxValue = null,
    minValue = null,
    type = type,
    options = emptyList(),
    selectedOptions = listOf(FieldOption(value = answer, text = answer, subtitle = null)),
    datePickerUiState = null,
    searchData = null,
  )

  private fun ComposeUiTest.showForm(vararg fields: StepContent.Form.Field) {
    val step = ClaimIntentStep(
      id = StepId("form"),
      text = "Var barnvagnen låst eller skyddad på något annat sätt?",
      stepContent = StepContent.Form(fields = fields.toList(), isSkippable = true),
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
      HedvigTheme(darkTheme = false) {
        Box(Modifier.requiredWidth(360.dp).fillMaxHeight()) {
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
    waitForIdle()
  }

  private companion object {
    const val LONG_ANSWER = "Ja, den var låst med ett kedjelås runt bakhjulet och fast i cykelstället i förrådet"
  }
}
