package com.hedvig.android.feature.terminateinsurance.step.withdrawal

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.contract.ContractGroup.HOMEOWNER
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.android.design.system.hedvig.withHedvigLink
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.AreYouSureSheet
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.InsuranceInfoCard
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardConfirmationEmail
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoCardDate
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import com.hedvig.android.feature.terminateinsurance.ui.TerminationSubmitError
import com.hedvig.android.feature.terminateinsurance.ui.TerminationSubmitErrorDialog
import hedvig.resources.Res
import hedvig.resources.TERMINATE_CONTRACT_TERMINATING_PROGRESS
import hedvig.resources.TERMINATION_FLOW_CONFIRMATION_SUBTITLE_TODAY
import hedvig.resources.TERMINATION_FLOW_SUMMARY_SUBTITLE
import hedvig.resources.TERMINATION_FLOW_WITHDRAWAL_BUTTON
import hedvig.resources.TERMINATION_FLOW_WITHDRAWAL_CHANGE_EMAIL_BODY_1
import hedvig.resources.TERMINATION_FLOW_WITHDRAWAL_CHANGE_EMAIL_BODY_2
import hedvig.resources.TERMINATION_FLOW_WITHDRAWAL_CHANGE_EMAIL_TITLE
import hedvig.resources.TERMINATION_FLOW_WITHDRAWAL_CONFIRM_BUTTON
import hedvig.resources.TERMINATION_FLOW_WITHDRAWAL_TITLE
import hedvig.resources.TERMINATION_KEEP_INSURANCE_BUTTON
import hedvig.resources.general_close_button
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TerminationWithdrawalDestination(
  viewModel: TerminationWithdrawalViewModel,
  navigateUp: () -> Unit,
  navigateToContactInfo: () -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TerminationWithdrawalScreen(
    uiState = uiState,
    onRetry = { viewModel.emit(TerminationWithdrawalEvent.Retry) },
    onConfirm = { viewModel.emit(TerminationWithdrawalEvent.Submit) },
    onDismissSubmitError = { viewModel.emit(TerminationWithdrawalEvent.DismissSubmitError) },
    navigateUp = navigateUp,
    navigateToContactInfo = navigateToContactInfo,
    closeTerminationFlow = closeTerminationFlow,
  )
}

@Composable
private fun TerminationWithdrawalScreen(
  uiState: TerminationWithdrawalUiState,
  onRetry: () -> Unit,
  onConfirm: () -> Unit,
  onDismissSubmitError: () -> Unit,
  navigateUp: () -> Unit,
  navigateToContactInfo: () -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  when (uiState) {
    TerminationWithdrawalUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    TerminationWithdrawalUiState.Failure -> {
      HedvigScaffold(navigateUp = navigateUp) {
        HedvigErrorSection(onButtonClick = onRetry, modifier = Modifier.weight(1f))
      }
    }

    is TerminationWithdrawalUiState.Content -> {
      TerminationWithdrawalContent(
        uiState = uiState,
        onConfirm = onConfirm,
        onDismissSubmitError = onDismissSubmitError,
        navigateUp = navigateUp,
        navigateToContactInfo = navigateToContactInfo,
        closeTerminationFlow = closeTerminationFlow,
      )
    }
  }
}

@Composable
private fun TerminationWithdrawalContent(
  uiState: TerminationWithdrawalUiState.Content,
  onConfirm: () -> Unit,
  onDismissSubmitError: () -> Unit,
  navigateUp: () -> Unit,
  navigateToContactInfo: () -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  if (uiState.submitError != null) {
    TerminationSubmitErrorDialog(uiState.submitError, onDismiss = onDismissSubmitError)
  }
  if (uiState.isSubmitting) {
    HedvigFullScreenCenterAlignedLinearProgress(
      title = stringResource(Res.string.TERMINATE_CONTRACT_TERMINATING_PROGRESS),
    )
    return
  }
  val formattedEndDate = rememberHedvigDateTimeFormatter().format(uiState.endDate)
  val areYouSureSheetState = rememberHedvigBottomSheetState<Unit>()
  AreYouSureSheet(
    state = areYouSureSheetState,
    description = stringResource(Res.string.TERMINATION_FLOW_CONFIRMATION_SUBTITLE_TODAY, formattedEndDate),
    confirmCancellation = onConfirm,
    confirmButtonText = stringResource(Res.string.TERMINATION_FLOW_WITHDRAWAL_CONFIRM_BUTTON),
  )
  val changeEmailSheetState = rememberHedvigBottomSheetState<Unit>()
  ChangeEmailBottomSheet(
    state = changeEmailSheetState,
    navigateToContactInfo = navigateToContactInfo,
  )
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
    modifier = Modifier.fillMaxSize(),
  ) {
    FlowHeading(
      stringResource(Res.string.TERMINATION_FLOW_WITHDRAWAL_TITLE),
      stringResource(Res.string.TERMINATION_FLOW_SUMMARY_SUBTITLE),
      Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f).heightIn(min = 8.dp))
    InsuranceInfoCard(
      insuranceInfo = uiState.insuranceInfo,
      extraCoverageItems = uiState.action.extraCoverageItems,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(4.dp))
    TerminationInfoCardDate(
      dateValue = formattedEndDate,
      onClick = null,
      isLocked = true,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(4.dp))
    TerminationInfoCardConfirmationEmail(
      email = uiState.confirmationEmail,
      onClick = { changeEmailSheetState.show() },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(4.dp))
    HedvigNotificationCard(
      message = uiState.statusText,
      priority = NotificationDefaults.NotificationPriority.Info,
      modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.TERMINATION_FLOW_WITHDRAWAL_BUTTON),
      enabled = true,
      onClick = { areYouSureSheetState.show() },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.TERMINATION_KEEP_INSURANCE_BUTTON),
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
private fun ChangeEmailBottomSheet(state: HedvigBottomSheetState<Unit>, navigateToContactInfo: () -> Unit) {
  HedvigBottomSheet(state) {
    HedvigText(
      text = stringResource(Res.string.TERMINATION_FLOW_WITHDRAWAL_CHANGE_EMAIL_TITLE),
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    val body = stringResource(Res.string.TERMINATION_FLOW_WITHDRAWAL_CHANGE_EMAIL_BODY_1)
    val linkText = stringResource(Res.string.TERMINATION_FLOW_WITHDRAWAL_CHANGE_EMAIL_BODY_2)
    HedvigText(
      text = buildAnnotatedString {
        append(body.substringBefore(LINK_PLACEHOLDER))
        withHedvigLink(
          tag = linkText,
          onClick = {
            state.dismiss()
            navigateToContactInfo()
          },
        ) {
          append(linkText)
        }
        append(body.substringAfter(LINK_PLACEHOLDER, missingDelimiterValue = ""))
      },
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_close_button),
      enabled = true,
      onClick = state::dismiss,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

private const val LINK_PLACEHOLDER = "%1\$s"

@HedvigPreview
@Composable
private fun PreviewTerminationWithdrawalScreen(
  @PreviewParameter(TerminationWithdrawalUiStateProvider::class) uiState: TerminationWithdrawalUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationWithdrawalScreen(
        uiState = uiState,
        onRetry = {},
        onConfirm = {},
        onDismissSubmitError = {},
        navigateUp = {},
        navigateToContactInfo = {},
        closeTerminationFlow = {},
      )
    }
  }
}

private class TerminationWithdrawalUiStateProvider :
  CollectionPreviewParameterProvider<TerminationWithdrawalUiState>(
    listOf(
      previewWithdrawalUiState(
        TerminationAction.TerminateWithDate(
          minDate = LocalDate(2026, 11, 25),
          maxDate = LocalDate(2026, 11, 25),
          extraCoverageItems = listOf(ExtraCoverageItem("Travel Plus", "45 dagar")),
        ),
        submitError = null,
      ),
      previewWithdrawalUiState(
        TerminationAction.DeleteInsurance(extraCoverageItems = emptyList()),
        submitError = TerminationSubmitError.Generic,
      ),
      previewWithdrawalUiState(
        TerminationAction.DeleteInsurance(extraCoverageItems = emptyList()),
        submitError = TerminationSubmitError.UserError("This insurance can no longer be withdrawn"),
      ),
      TerminationWithdrawalUiState.Loading,
      TerminationWithdrawalUiState.Failure,
    ),
  )

private fun previewWithdrawalUiState(action: TerminationAction, submitError: TerminationSubmitError?) =
  TerminationWithdrawalUiState.Content(
    insuranceInfo = TerminationGraphParameters(
      contractId = "contractId",
      insuranceDisplayName = "Hemförsäkring",
      exposureName = "Bellmansgatan 19",
      contractGroup = HOMEOWNER,
    ),
    statusText = "Din försäkring är aktiv sedan 20 nov. 2026",
    action = action,
    endDate = LocalDate(2026, 11, 25),
    confirmationEmail = "member@example.com",
    submitError = submitError,
    isSubmitting = false,
  )
