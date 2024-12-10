package com.hedvig.android.feature.terminateinsurance.step.terminationreview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.contract.ContractGroup.HOMEOWNER
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination.TerminationConfirmation.TerminationType
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun TerminationConfirmationDestination(
  viewModel: TerminationConfirmationViewModel,
  navigateToNextStep: (TerminateInsuranceStep) -> Unit,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep == null) return@LaunchedEffect
    navigateToNextStep(nextStep)
  }

  TerminationConfirmationScreen(
    uiState = uiState,
    onContinue = onContinue,
    navigateBack = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  )
}

@Composable
private fun TerminationConfirmationScreen(
  uiState: OverviewUiState,
  onContinue: () -> Unit,
  navigateBack: () -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  val isSubmittingTerminationOrNavigatingForward = uiState.isSubmittingContractTermination || uiState.nextStep != null
  if (isSubmittingTerminationOrNavigatingForward) {
    HedvigFullScreenCenterAlignedLinearProgress(
      title = stringResource(id = R.string.TERMINATE_CONTRACT_TERMINATING_PROGRESS),
    )
  } else {
    AreYouSureScreen(
      type = uiState.terminationType,
      insuranceInfo = uiState.insuranceInfo,
      extraCoverageItems = uiState.extraCoverageItems,
      modifier = Modifier.fillMaxSize(),
      navigateUp = navigateBack,
      closeTerminationFlow = closeTerminationFlow,
      onContinue = onContinue,
    )
  }
}

@Composable
private fun AreYouSureScreen(
  type: TerminationType,
  insuranceInfo: TerminationGraphParameters,
  extraCoverageItems: List<ExtraCoverageItem>,
  modifier: Modifier = Modifier,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onContinue: () -> Unit,
) {
  val areYouSureSheetState = rememberHedvigBottomSheetState<Unit>()
  AreYouSureSheet(state = areYouSureSheetState, type = type, confirmCancellation = onContinue)
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = closeTerminationFlow,
        content = {
          Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = null,
          )
        },
      )
    },
  ) {
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = stringResource(id = R.string.TERMINATION_FLOW_CANCELLATION_TITLE),
      style = HedvigTheme.typography.headlineMedium,
      modifier = modifier.padding(horizontal = 16.dp),
    )
    HedvigText(
      text = stringResource(id = R.string.TERMINATION_FLOW_SUMMARY_SUBTITLE),
      style = HedvigTheme.typography.headlineMedium,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = modifier.padding(horizontal = 16.dp),
    )
    Spacer(
      Modifier
        .weight(1f)
        .heightIn(min = 8.dp),
    )
    InsuranceInfoCard(
      insuranceInfo = insuranceInfo,
      extraCoverageItems = extraCoverageItems,
      modifier = modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.TERMINATION_BUTTON),
      enabled = true,
      onClick = { areYouSureSheetState.show() },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.TERMINATION_KEEP_INSURANCE_BUTTON),
      buttonSize = Large,
      onClick = closeTerminationFlow,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun AreYouSureSheet(
  state: HedvigBottomSheetState<Unit>,
  type: TerminationType,
  confirmCancellation: () -> Unit,
) {
  HedvigBottomSheet(state) {
    AreYouSureSheetContent(
      type = type,
      confirmCancellation = {
        state.dismiss()
        confirmCancellation()
      },
      dismissSheet = state::dismiss,
    )
  }
}

@Composable
private fun AreYouSureSheetContent(
  type: TerminationType,
  confirmCancellation: () -> Unit,
  dismissSheet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  Column(modifier) {
    EmptyState(
      iconStyle = ERROR,
      text = stringResource(id = R.string.GENERAL_ARE_YOU_SURE),
      description = when (type) {
        TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion ->
          stringResource(id = R.string.TERMINATION_FLOW_CONFIRMATION)

        is Termination ->
          stringResource(
            id = R.string.TERMINATION_FLOW_CONFIRMATION_SUBTITLE_TERMINATION,
            dateTimeFormatter.format(type.terminationDate.toJavaLocalDate()),
          )
      },
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.TERMINATION_FLOW_CONFIRM_BUTTON),
      onClick = confirmCancellation,
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(R.string.general_close_button),
      onClick = dismissSheet,
      enabled = true,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun InsuranceInfoCard(
  insuranceInfo: TerminationGraphParameters,
  extraCoverageItems: List<ExtraCoverageItem>,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier) {
    Column(Modifier.padding(16.dp)) {
      Row {
        Image(
          painter = painterResource(insuranceInfo.contractGroup.toPillow()),
          contentDescription = null,
          modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
          HedvigText(insuranceInfo.insuranceDisplayName)
          HedvigText(insuranceInfo.exposureName, color = HedvigTheme.colorScheme.textSecondary)
        }
      }
      if (extraCoverageItems.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        HedvigText(stringResource(R.string.TERMINATION_ADDON_COVERAGE_TITLE))
        ProvideTextStyle(LocalTextStyle.current.copy(color = HedvigTheme.colorScheme.textSecondary)) {
          for (extraCoverageItem in extraCoverageItems) {
            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = { HedvigText(extraCoverageItem.displayName) },
              endSlot = {
                HedvigText(
                  text = extraCoverageItem.displayValue ?: "",
                  textAlign = TextAlign.End,
                  modifier = Modifier.wrapContentSize(Alignment.TopEnd),
                )
              },
              spaceBetween = 4.dp,
            )
          }
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationConfirmationScreen(
  @PreviewParameter(PreviewTerminationConfirmationScreenProvider::class) input: Triple<Boolean, Boolean, Boolean>,
) {
  val terminationDate = LocalDate(2024, 8, 9)
  val isLoading = input.first
  val type = when (input.second) {
    false -> TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion
    true -> TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination(terminationDate)
  }
  val withExtraCoverage = input.third
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationConfirmationScreen(
        uiState = OverviewUiState(
          terminationType = type,
          insuranceInfo = TerminationGraphParameters(
            insuranceDisplayName = "insuranceDisplayName",
            exposureName = "exposureName",
            HOMEOWNER,
          ),
          extraCoverageItems = List(if (withExtraCoverage) 2 else 0) {
            ExtraCoverageItem(displayName = "displayName#$it", displayValue = "displayValue#$it")
          },
          nextStep = null,
          errorMessage = null,
          isSubmittingContractTermination = isLoading,
        ),
        onContinue = {},
        navigateBack = {},
        closeTerminationFlow = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAreYouSureSheetContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AreYouSureSheetContent(
        type = Termination(LocalDate(2024, 8, 9)),
        confirmCancellation = {},
        dismissSheet = {},
      )
    }
  }
}

private class PreviewTerminationConfirmationScreenProvider :
  CollectionPreviewParameterProvider<Triple<Boolean, Boolean, Boolean>>(
    listOf(
      Triple(true, false, false),
      Triple(false, false, false),
      Triple(false, true, true),
      Triple(false, true, false),
    ),
  )
