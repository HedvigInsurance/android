package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LockedState.Locked
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplay
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion.Action.DowngradePriceByChangingTier
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion.Action.UnknownAction
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion.Action.UpdateAddress
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion.Action.UpgradeCoverageByChangingTier
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion.Redirect
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.data.TerminationReason
import com.hedvig.android.feature.terminateinsurance.data.TerminationSurveyOption
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R

@Composable
internal fun TerminationSurveyDestination(
  viewModel: TerminationSurveyViewModel,
  navigateUp: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  closeTerminationFlow: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToNextStep: (step: TerminateInsuranceStep) -> Unit,
  navigateToSubOptions: ((List<TerminationSurveyOption>) -> Unit)?,
  redirectToChangeTierFlow: (Pair<String, ChangeTierDeductibleIntent>) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.intentAndIdToRedirectToChangeTierFlow) {
    val intent = uiState.intentAndIdToRedirectToChangeTierFlow
    if (intent != null) {
      viewModel.emit(TerminationSurveyEvent.ClearNextStep)
      redirectToChangeTierFlow(intent)
    }
  }
  LaunchedEffect(uiState.nextNavigationStep) {
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
    navigateToMovingFlow = navigateToMovingFlow,
    closeTerminationFlow = closeTerminationFlow,
    onContinueClick = { viewModel.emit(TerminationSurveyEvent.Continue) },
    selectOption = { option ->
      viewModel.emit(TerminationSurveyEvent.SelectOption(option))
    },
    changeFeedbackForSelectedReason = { feedback ->
      viewModel.emit(TerminationSurveyEvent.ChangeFeedbackForSelectedReason(feedback))
    },
    onCloseFullScreenEditText = {
      viewModel.emit(TerminationSurveyEvent.CloseFullScreenEditText)
    },
    onLaunchFullScreenEditText = {
      viewModel.emit(TerminationSurveyEvent.ShowFullScreenEditText(it))
    },
    openUrl = openUrl,
    tryToDowngradePrice = {
      viewModel.emit(TerminationSurveyEvent.TryToDowngradePrice)
    },
    tryToUpgradeCoverage = {
      viewModel.emit(TerminationSurveyEvent.TryToUpgradeCoverage)
    },
    closeEmptyQuotesDialog = {
      viewModel.emit(TerminationSurveyEvent.ClearEmptyQuotes)
    },
  )
}

@Composable
private fun TerminationSurveyScreen(
  uiState: TerminationSurveyState,
  selectOption: (TerminationSurveyOption) -> Unit,
  navigateUp: () -> Unit,
  navigateToMovingFlow: () -> Unit,
  closeTerminationFlow: () -> Unit,
  openUrl: (String) -> Unit,
  onCloseFullScreenEditText: () -> Unit,
  onLaunchFullScreenEditText: (option: TerminationSurveyOption) -> Unit,
  changeFeedbackForSelectedReason: (feedback: String?) -> Unit,
  onContinueClick: () -> Unit,
  tryToUpgradeCoverage: () -> Unit,
  tryToDowngradePrice: () -> Unit,
  closeEmptyQuotesDialog: () -> Unit,
) {
  FreeTextOverlay(
    freeTextMaxLength = 2000,
    freeTextValue = uiState.showFullScreenEditText?.feedBack,
    freeTextHint = stringResource(id = R.string.TERMINATION_SURVEY_FEEDBACK_HINT),
    freeTextOnCancelClick = {
      onCloseFullScreenEditText()
    },
    freeTextOnSaveClick = { feedback ->
      changeFeedbackForSelectedReason(feedback)
    },
    shouldShowOverlay = uiState.showFullScreenEditText != null,
    overlaidContent = {
      TerminationScaffold(
        navigateUp = navigateUp,
        closeTerminationFlow = closeTerminationFlow,
      ) {
        if (uiState.showEmptyQuotesDialog) {
          HedvigDialog(
            onDismissRequest = closeEmptyQuotesDialog,
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(
                  WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal +
                      WindowInsetsSides.Bottom,
                  ),
                ),
            ) {
              Spacer(Modifier.weight(1f))
              EmptyState(
                text = stringResource(R.string.TERMINATION_NO_TIER_QUOTES_SUBTITLE),
                iconStyle = INFO,
                buttonStyle = NoButton,
                modifier = Modifier.fillMaxWidth(),
                description = null,
              )
              Spacer(Modifier.weight(1f))
              HedvigTextButton(
                stringResource(R.string.general_close_button),
                onClick = closeEmptyQuotesDialog,
                buttonSize = Large,
                modifier = Modifier.fillMaxWidth(),
              )
              Spacer(Modifier.height(32.dp))
            }
          }
        }
        HedvigText(
          style = HedvigTheme.typography.headlineMedium.copy(
            lineBreak = LineBreak.Heading,
            color = HedvigTheme.colorScheme.textSecondary,
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
          Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
          ) {
            val subTitle = stringResource(R.string.GENERAL_ERROR_BODY)
            val title = stringResource(R.string.GENERAL_ERROR_BODY)
            EmptyState(
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .wrapContentWidth(),
              text = title,
              iconStyle = ERROR,
              description = subTitle,
            )
            Spacer(Modifier.height(16.dp))
          }
        }
        for (reason in uiState.reasons) {
          Column {
            RadioOption(
              optionText = reason.surveyOption.title,
              chosenState = if (uiState.selectedOption == reason.surveyOption) Chosen else NotChosen,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              onClick = {
                if (!reason.surveyOption.isDisabled) {
                  selectOption(reason.surveyOption)
                }
              },
              lockedState = if (reason.surveyOption.isDisabled) Locked else NotLocked,
              radioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
            )
            Spacer(modifier = (Modifier.height(4.dp)))
            AnimatedVisibility(
              visible = (
                reason.surveyOption == uiState.selectedOption &&
                  !reason.surveyOption.isDisabled
              ),
            ) {
              Column {
                val suggestion = reason.surveyOption.suggestion
                if (suggestion != null && suggestion != UnknownAction) {
                  val text = suggestion.description
                  val buttonText = suggestion.buttonTitle
                  val onSuggestionButtonClick: () -> Unit = when (suggestion) {
                    is UpdateAddress -> {
                      dropUnlessResumed { navigateToMovingFlow() }
                    }

                    is Redirect -> {
                      { openUrl(suggestion.url) }
                    }

                    is DowngradePriceByChangingTier -> {
                      {
                        tryToDowngradePrice()
                      }
                    }

                    is UpgradeCoverageByChangingTier -> {
                      {
                        tryToUpgradeCoverage()
                      }
                    }

                    UnknownAction -> {
                      {}
                    }
                  }
                  HedvigNotificationCard(
                    buttonLoading = uiState.actionButtonLoading,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    message = text,
                    priority = NotificationPriority.Campaign,
                    style = InfoCardStyle.Button(
                      buttonText = buttonText,
                      onButtonClick = onSuggestionButtonClick,
                    ),
                  )
                  Spacer(modifier = (Modifier.height(4.dp)))
                }
                if (reason.surveyOption.feedBackRequired) {
                  FreeTextDisplay(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {
                      onLaunchFullScreenEditText(reason.surveyOption)
                    },
                    freeTextValue = reason.feedBack,
                    freeTextPlaceholder = stringResource(id = R.string.TERMINATION_SURVEY_FEEDBACK_HINT),
                  )
                  Spacer(modifier = (Modifier.height(4.dp)))
                }
              }
            }
          }
        }
        Spacer(Modifier.height(12.dp))
        Row(
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth(),
        ) {
          HedvigButton(
            stringResource(id = R.string.general_continue_button),
            enabled = uiState.continueAllowed,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            onClick = onContinueClick,
            isLoading = uiState.navigationStepLoadingForReason != null,
          )
        }
        Spacer(Modifier.height(16.dp))
      }
    },
  )
}

@HedvigPreview
@Composable
private fun ShowSurveyScreenPreview(
  @PreviewParameter(
    ShowSurveyUiStateProvider::class,
  ) uiState: TerminationSurveyState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationSurveyScreen(
        uiState = uiState,
        selectOption = {},
        navigateUp = {},
        navigateToMovingFlow = {},
        closeTerminationFlow = {},
        changeFeedbackForSelectedReason = {},
        onContinueClick = {},
        onCloseFullScreenEditText = {},
        onLaunchFullScreenEditText = {},
        openUrl = {},
        tryToDowngradePrice = {},
        tryToUpgradeCoverage = {},
        closeEmptyQuotesDialog = {},
      )
    }
  }
}

private class ShowSurveyUiStateProvider :
  CollectionPreviewParameterProvider<TerminationSurveyState>(
    listOf(
      TerminationSurveyState(
        nextNavigationStep = null,
        navigationStepLoadingForReason = null,
        selectedOption = previewReason1.surveyOption,
        reasons = listOf(previewReason1, previewReason2, previewReason3),
      ),
      TerminationSurveyState(
        nextNavigationStep = null,
        navigationStepLoadingForReason = null,
        selectedOption = previewReason3.surveyOption,
        reasons = listOf(previewReason1, previewReason2, previewReason3),
      ),
      TerminationSurveyState(
        nextNavigationStep = null,
        navigationStepLoadingForReason = null,
        errorWhileLoadingNextStep = false,
        selectedOption = previewReason2.surveyOption,
        reasons = listOf(previewReason1, previewReason2, previewReason3),
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
        listIndex = 0,
      ),
      TerminationSurveyOption(
        id = "12",
        title = "I'm moving abroad",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = false,
        listIndex = 1,
      ),
      TerminationSurveyOption(
        id = "23",
        title = "Other",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
        listIndex = 2,
      ),
    ),
    suggestion = SurveyOptionSuggestion.Action.UpdateAddress("test description", "test"),
    feedBackRequired = true,
    listIndex = 0,
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
    listIndex = 1,
  ),
  feedBack = LoremIpsum(25).values.first(),
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
        listIndex = 0,
      ),
      TerminationSurveyOption(
        id = "32",
        title = "I am dissatisfied with the service",
        subOptions = listOf(),
        suggestion = null,
        feedBackRequired = true,
        listIndex = 1,
      ),
    ),
    suggestion = SurveyOptionSuggestion.Redirect(
      "http://www.google.com",
      "Do this action instead",
      "Click here to do it",
    ),
    feedBackRequired = false,
    listIndex = 3,
  ),
  null,
)
