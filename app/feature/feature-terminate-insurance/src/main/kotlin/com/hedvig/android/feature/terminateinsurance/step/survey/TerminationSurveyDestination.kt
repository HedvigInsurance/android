package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.borderSecondary
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.core.ui.text.WarningTextWithIcon
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationReason
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R

@Composable
internal fun TerminationSurveyDestination(
  viewModel: TerminationSurveyViewModel,
  navigateUp: () -> Unit,
  openChat: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  closeTerminationFlow: () -> Unit,
  navigateToNextStep: (step: TerminateInsuranceStep) -> Unit,
  navigateToSubOptions: ((List<TerminationSurveyOption>) -> Unit)?,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState) {
    val nextStep = uiState.nextNavigationStep
    if (nextStep != null) {
      when (nextStep) {
        is SurveyNavigationStep.NavigateToNextTerminationStep -> {
          viewModel.emit(TerminationSurveyEvent.ClearNextStep)
          navigateToNextStep(nextStep.step)
        }
        SurveyNavigationStep.NavigateToSubOptions -> {
          viewModel.emit(TerminationSurveyEvent.ClearNextStep)
          uiState.selectedOption?.let {
            navigateToSubOptions?.invoke(it.subOptions)
          }
        }
      }
    }
  }
  TerminationSurveyScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    openChat = openChat,
    navigateToMovingFlow = navigateToMovingFlow,
    closeTerminationFlow = closeTerminationFlow,
    onContinueClick = { viewModel.emit(TerminationSurveyEvent.Continue) },
    selectOption = { option ->
      viewModel.emit(TerminationSurveyEvent.SelectOption(option))
    },
    changeFeedbackForReason = { option, feedback ->
      viewModel.emit(TerminationSurveyEvent.ChangeFeedbackForReason(option, feedback))
    },
  )
}

@Composable
private fun TerminationSurveyScreen(
  uiState: TerminationSurveyState,
  selectOption: (TerminationSurveyOption) -> Unit,
  navigateUp: () -> Unit,
  openChat: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  closeTerminationFlow: () -> Unit,
  changeFeedbackForReason: (option: TerminationSurveyOption, feedback: String) -> Unit,
  onContinueClick: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) {
    Text(
      style = MaterialTheme.typography.headlineSmall.copy(
        lineBreak = LineBreak.Heading,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      ),
      text = stringResource(id = R.string.TERMINATION_SURVEY_SUBTITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    AnimatedVisibility(
      visible = uiState.errorWhileLoadingNextStep,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Column {
        WarningTextWithIcon(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .wrapContentWidth(),
          text = stringResource(R.string.something_went_wrong),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
    for (reason in uiState.reasons) {
      HedvigCard(
        onClick = { selectOption(reason.surveyOption) },
        colors = CardDefaults.outlinedCardColors(
          containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .heightIn(64.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
          Text(
            text = reason.surveyOption.title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
          )
          Spacer(Modifier.width(8.dp))
          SelectIndicationCircle(
            uiState.selectedOption == reason.surveyOption,
            selectedIndicationColor = MaterialTheme.colorScheme.typeElement,
            unselectedCircleColor = MaterialTheme.colorScheme.borderSecondary,
          )
        }
      }
      Spacer(modifier = (Modifier.height(4.dp)))
    }
    Spacer(Modifier.height(12.dp))
    HedvigContainedButton(
      stringResource(id = R.string.general_continue_button),
      enabled = uiState.continueAllowed,
      modifier = Modifier.padding(horizontal = 16.dp),
      colors = ButtonDefaults.buttonColors(
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      ),
      onClick = onContinueClick,
      isLoading = uiState.isNavigationStepLoading,
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun ShowSurveyScreenPreview(
  @PreviewParameter(
    ShowSurveyUiStateProvider::class,
  ) uiState: TerminationSurveyState,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationSurveyScreen(
        uiState,
        {},
        {},
        {},
        {},
        {},
        { option, String ->
        },
        {},
      )
    }
  }
}

private class ShowSurveyUiStateProvider :
  CollectionPreviewParameterProvider<TerminationSurveyState>(
    listOf(
      TerminationSurveyState(
        nextNavigationStep = null,
        isNavigationStepLoading = false,
        feedbackEmptyWarning = false,
        selectedOption = previewReason2.surveyOption,
        reasons = listOf(previewReason1, previewReason2, previewReason3),
      ),
      TerminationSurveyState(
        nextNavigationStep = null,
        isNavigationStepLoading = false,
        feedbackEmptyWarning = true,
        selectedOption = previewReason2.surveyOption,
        reasons = listOf(previewReason1, previewReason2, previewReason3),
      ),
//      TerminationSurveyState(
//          nextNavigationStep = null,
//          isNavigationStepLoading = true,
//          feedbackEmptyWarning = false,
//          selectedOption = previewReason2.surveyOption,
//          reasons = listOf(previewReason1, previewReason2filled, previewReason3),
//      ),
      TerminationSurveyState(
        nextNavigationStep = null,
        isNavigationStepLoading = false,
        errorWhileLoadingNextStep = true,
        feedbackEmptyWarning = false,
        selectedOption = previewReason2.surveyOption,
        reasons = listOf(previewReason1, previewReason2filled, previewReason3),
      ),
    ),
  )

private val previewReason1 = TerminationReason(
  TerminationSurveyOption(
    id = "1",
    title = "I'm moving",
    subOptions = listOf(
      TerminationSurveyOption(
        id = "11",
        title = "I'm moving in with someone else",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = false,
      ),
      TerminationSurveyOption(
        id = "12",
        title = "I'm moving abroad",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = false,
      ),
      TerminationSurveyOption(
        id = "23",
        title = "Other",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
      ),
    ),
    suggestion = SurveyOptionSuggestion.Action.UPDATE_ADDRESS,
    feedBackRequired = false,
  ),
  null,
)

private val previewReason2 = TerminationReason(
  TerminationSurveyOption(
    id = "2",
    title = "I got a better offer elsewhere",
    subOptions = listOf(),
    suggestion = null,
    feedBackRequired = true,
  ),
  null,
)

private val previewReason2filled = TerminationReason(
  TerminationSurveyOption(
    id = "2",
    title = "I got a better offer elsewhere",
    subOptions = listOf(),
    suggestion = null,
    feedBackRequired = true,
  ),
  "Got a great all included offer from If",
)

private val previewReason3 = TerminationReason(
  TerminationSurveyOption(
    id = "3",
    title = "I am dissatisfied",
    subOptions = listOf(
      TerminationSurveyOption(
        id = "31",
        title = "I am dissatisfied with the coverage",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
      ),
      TerminationSurveyOption(
        id = "32",
        title = "I am dissatisfied with the service",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
      ),
    ),
    suggestion = null,
    feedBackRequired = false,
  ),
  null,
)
