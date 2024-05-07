package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.material3.borderSecondary
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateStepUiState
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateStepUiStateProvider
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R

@Composable
internal fun TerminationSurveyDestination(
  viewModel: TerminationSurveyViewModel,
  navigateUp: () -> Unit,
  openChat: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  closeTerminationFlow: () -> Unit,
  navigateToNextStep: (
    step: TerminateInsuranceStep,
  ) -> Unit,
  navigateToSubOptions: ((List<TerminationSurveyOption>) -> Unit)?,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState) {
    val uiStateValue = uiState as? TerminationSurveyState.ShowSurvey ?: return@LaunchedEffect
    if (uiStateValue.nextStep != null) {
      viewModel.emit(TerminationSurveyEvent.ClearNextStep)
      navigateToNextStep(uiStateValue.nextStep)
    } else if (uiStateValue.navigateToSubOptions) {
      viewModel.emit(TerminationSurveyEvent.ClearNextStep)
      uiStateValue.selectedOption?.let {
        navigateToSubOptions?.invoke(it.subOptions)
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
    retry = { viewModel.emit(TerminationSurveyEvent.Retry) },
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
  retry: () -> Unit,
  navigateUp: () -> Unit,
  openChat: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  closeTerminationFlow: () -> Unit,
  changeFeedbackForReason: (option: TerminationSurveyOption, feedback: String) -> Unit,
  onContinueClick: () -> Unit,
) {
  when (uiState) {
    TerminationSurveyState.ErrorWhileSubmittingReason -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = retry, modifier = Modifier.weight(1f))
      }
    }

    TerminationSurveyState.Loading -> HedvigFullScreenCenterAlignedProgress()
    is TerminationSurveyState.ShowSurvey -> ShowSurveyScreen(
      uiState = uiState,
      selectOption = selectOption,
      navigateUp = navigateUp,
      openChat = openChat,
      navigateToMovingFlow = navigateToMovingFlow,
      closeTerminationFlow = closeTerminationFlow,
      changeFeedbackForReason = changeFeedbackForReason,
      onContinueClick = onContinueClick,
    )
  }
}

@Composable
private fun ShowSurveyScreen(
  uiState: TerminationSurveyState.ShowSurvey,
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
//    AnimatedVisibility(
//      visible = uiState.navigationStepFailedToLoad,
//      enter = fadeIn(),
//      exit = fadeOut(),
//    ) {
//      Column {
//        WarningTextWithIcon(
//          modifier = Modifier
//            .padding(horizontal = 16.dp)
//            .fillMaxWidth()
//            .wrapContentWidth(),
//          text = stringResource(R.string.something_went_wrong),
//        )
//        Spacer(Modifier.height(16.dp))
//      }
//    } //todo: add to uiState
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
    SurveyUiStateProvider::class,
  ) uiState: TerminationSurveyState.ShowSurvey,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ShowSurveyScreen(
        uiState,
        {}, {}, {}, {}, {},
        { option, String ->
        },
        {}
      )
    }
  }
}

private class SurveyUiStateProvider :
  CollectionPreviewParameterProvider<TerminationSurveyState.ShowSurvey>(
    listOf(
      TerminationSurveyState.ShowSurvey(

      )
    )
  )
