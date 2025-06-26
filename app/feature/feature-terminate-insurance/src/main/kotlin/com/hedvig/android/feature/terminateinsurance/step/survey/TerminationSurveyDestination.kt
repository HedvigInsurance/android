package com.hedvig.android.feature.terminateinsurance.step.survey

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.halilibo.richtext.commonmark.Markdown
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
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LockedState.Locked
import com.hedvig.android.design.system.hedvig.LockedState.NotLocked
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults
import com.hedvig.android.design.system.hedvig.RichText
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplay
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay
import com.hedvig.android.feature.terminateinsurance.data.InfoType
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion.Known.Action.DowngradePriceByChangingTier
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion.Known.Action.Redirect
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion.Known.Action.UpdateAddress
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionSuggestion.Known.Action.UpgradeCoverageByChangingTier
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
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
      viewModel.emit(TerminationSurveyEvent.EditTextFeedback(feedback))
    },
    onCloseFullScreenEditText = {
      viewModel.emit(TerminationSurveyEvent.CloseFullScreenEditText)
    },
    onLaunchFullScreenEditText = {
      viewModel.emit(TerminationSurveyEvent.ShowFullScreenEditText)
    },
    openUrl = openUrl,
    tryToDowngradePrice = {
      viewModel.emit(TerminationSurveyEvent.TryToDowngradePrice)
    },
    tryToUpgradeCoverage = {
      viewModel.emit(TerminationSurveyEvent.TryToUpgradeCoverage)
    },
    closeEmptyQuotesDialog = {
      viewModel.emit(TerminationSurveyEvent.ClearEmptyQuotesDialog)
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
  onLaunchFullScreenEditText: () -> Unit,
  changeFeedbackForSelectedReason: (feedback: String?) -> Unit,
  onContinueClick: () -> Unit,
  tryToUpgradeCoverage: () -> Unit,
  tryToDowngradePrice: () -> Unit,
  closeEmptyQuotesDialog: () -> Unit,
) {
  FreeTextOverlay(
    freeTextMaxLength = 2000,
    freeTextValue = uiState.feedbackText,
    freeTextHint = stringResource(id = R.string.TERMINATION_SURVEY_FEEDBACK_POPOVER_HINT),
    freeTextTitle = stringResource(id = R.string.TERMINATION_SURVEY_FEEDBACK_HINT),
    freeTextOnCancelClick = {
      onCloseFullScreenEditText()
    },
    freeTextOnSaveClick = { feedback ->
      changeFeedbackForSelectedReason(feedback)
    },
    shouldShowOverlay = uiState.showFullScreenEditText,
    overlaidContent = {
      TerminationScaffold(
        navigateUp = navigateUp,
        closeTerminationFlow = closeTerminationFlow,
      ) { title ->
        if (uiState.showEmptyQuotesDialog || true) {
          HedvigDialog(
            onDismissRequest = closeEmptyQuotesDialog,
            contentPadding = PaddingValues(0.dp),
          ) {
            EmptyQuotesDialogContent(closeEmptyQuotesDialog)
          }
        }
        FlowHeading(
          title,
          stringResource(id = R.string.TERMINATION_SURVEY_SUBTITLE),
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
            EmptyState(
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .wrapContentWidth(),
              text = stringResource(R.string.something_went_wrong),
              iconStyle = ERROR,
              description = stringResource(R.string.GENERAL_ERROR_BODY),
            )
            Spacer(Modifier.height(16.dp))
          }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          for (reason in uiState.reasons) {
            RadioOption(
              optionText = reason.title,
              chosenState = if (uiState.selectedOption == reason) Chosen else NotChosen,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              onClick = {
                if (!reason.isDisabled) {
                  selectOption(reason)
                }
              },
              lockedState = if (reason.isDisabled) Locked else NotLocked,
              radioOptionSize = RadioOptionDefaults.RadioOptionSize.Medium,
            )
          }
        }
        SelectedSurveyInfoBox(
          selectedOption = uiState.selectedOption,
          actionButtonLoading = uiState.actionButtonLoading,
          navigateToMovingFlow = navigateToMovingFlow,
          openUrl = openUrl,
          tryToDowngradePrice = tryToDowngradePrice,
          tryToUpgradeCoverage = tryToUpgradeCoverage,
          modifier = Modifier.fillMaxWidth(),
        )
        SelectedSurveyTextDisplay(
          selectedReason = uiState.selectedOption,
          feedbackText = uiState.feedbackText,
          onLaunchFullScreenEditText = onLaunchFullScreenEditText,
          modifier = Modifier.fillMaxWidth(),
        )
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
            isLoading = uiState.navigationStepLoading,
          )
        }
        Spacer(Modifier.height(16.dp))
      }
    },
  )
}

@Composable
private fun ColumnScope.SelectedSurveyInfoBox(
  selectedOption: TerminationSurveyOption?,
  actionButtonLoading: Boolean,
  navigateToMovingFlow: () -> Unit,
  openUrl: (String) -> Unit,
  tryToDowngradePrice: () -> Unit,
  tryToUpgradeCoverage: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedContent(
    targetState = selectedOption,
    transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() },
    modifier = modifier,
  ) { selectedReason ->
    if (
      selectedReason != null &&
      selectedReason.suggestion != null &&
      !selectedReason.isDisabled &&
      selectedReason.suggestion is SurveyOptionSuggestion.Known
    ) {
      Column {
        val suggestion = selectedReason.suggestion
        Spacer(Modifier.height(4.dp))
        HedvigNotificationCard(
          buttonLoading = actionButtonLoading,
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
          content = {
            ProvideTextStyle(
              HedvigTheme.typography.label,
            ) {
              RichText {
                Markdown(
                  content = suggestion.description,
                )
              }
            }
          },
          priority = when (suggestion.infoType) {
            InfoType.INFO -> NotificationPriority.Info
            InfoType.OFFER -> NotificationPriority.Campaign
            InfoType.UNKNOWN -> NotificationPriority.InfoInline
          },
          style = when (suggestion) {
            is SurveyOptionSuggestion.Known.Action -> InfoCardStyle.Button(
              buttonText = suggestion.buttonTitle,
              onButtonClick = when (suggestion) {
                is DowngradePriceByChangingTier -> tryToDowngradePrice
                is UpdateAddress -> dropUnlessResumed { navigateToMovingFlow() }
                is UpgradeCoverageByChangingTier -> tryToUpgradeCoverage
                is Redirect -> {
                  { openUrl(suggestion.url) }
                }
              },
            )

            is SurveyOptionSuggestion.Known.Info -> InfoCardStyle.Default
          },
        )
        Spacer(modifier = (Modifier.height(4.dp)))
      }
    } else {
      Spacer(Modifier)
    }
  }
}

@Composable
private fun ColumnScope.SelectedSurveyTextDisplay(
  selectedReason: TerminationSurveyOption?,
  feedbackText: String?,
  onLaunchFullScreenEditText: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val showTextEntry: (TerminationSurveyOption?) -> Boolean = { reason ->
    reason != null &&
      !reason.isDisabled &&
      reason.feedBackRequired
  }
  AnimatedContent(
    targetState = selectedReason,
    transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() },
    contentKey = { showTextEntry(it) },
    modifier = modifier,
  ) { selectedReason ->
    if (showTextEntry(selectedReason)) {
      Column {
        Spacer(modifier = (Modifier.height(4.dp)))
        FreeTextDisplay(
          modifier = Modifier.padding(horizontal = 16.dp),
          onClick = { onLaunchFullScreenEditText() },
          freeTextValue = feedbackText,
          freeTextPlaceholder = stringResource(id = R.string.TERMINATION_SURVEY_FEEDBACK_HINT),
        )
      }
    } else {
      Spacer(Modifier)
    }
  }
}

@Composable
private fun EmptyQuotesDialogContent(closeEmptyQuotesDialog: () -> Unit) {
  Column {
    EmptyState(
      text = stringResource(R.string.TERMINATION_NO_TIER_QUOTES_SUBTITLE),
      iconStyle = INFO,
      buttonStyle = NoButton,
      modifier = Modifier.fillMaxWidth(),
      description = null,
    )
    HedvigTextButton(
      stringResource(R.string.general_close_button),
      onClick = closeEmptyQuotesDialog,
      buttonSize = Large,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationSurveyScreen(
  @PreviewParameter(ShowSurveyUiStateProvider::class) uiState: TerminationSurveyState,
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

@HedvigPreview
@Composable
private fun PreviewEmptyQuotesDialogContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      EmptyQuotesDialogContent({})
    }
  }
}

private class ShowSurveyUiStateProvider :
  CollectionPreviewParameterProvider<TerminationSurveyState>(
    listOf(
      TerminationSurveyState(reasons = listOf(previewReason1, previewReason2, previewReason3)).copy(
        showFullScreenEditText = false,
        selectedOptionId = previewReason1.id,
        errorWhileLoadingNextStep = false,
      ),
      TerminationSurveyState(reasons = listOf(previewReason1, previewReason2, previewReason3)).copy(
        showFullScreenEditText = false,
        selectedOptionId = previewReason2.id,
        errorWhileLoadingNextStep = false,
      ),
      TerminationSurveyState(reasons = listOf(previewReason1, previewReason2, previewReason3)).copy(
        showFullScreenEditText = false,
        selectedOptionId = previewReason3.id,
        errorWhileLoadingNextStep = true,
      ),
    ),
  )

private val previewReason1 = TerminationSurveyOption(
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
  suggestion = SurveyOptionSuggestion.Known.Info(
    "Why don't you try this: go to [Move to a new address](https://hedvig.page.link/home) here in the app, then proceed from there as you see fit",
    infoType = InfoType.OFFER,
  ),
  feedBackRequired = true,
  listIndex = 0,
)

private val previewReason2 = TerminationSurveyOption(
  id = "2",
  title = "I got a better offer elsewhere",
  subOptions = listOf(),
  suggestion = null,
  feedBackRequired = true,
  listIndex = 1,
)

private val previewReason3 = TerminationSurveyOption(
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
  suggestion = SurveyOptionSuggestion.Known.Action.Redirect(
    "http://www.google.com",
    "Do this action instead",
    "Click here to do it",
    infoType = InfoType.OFFER,
  ),
  feedBackRequired = false,
  listIndex = 3,
)
