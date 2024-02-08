package com.hedvig.android.feature.terminateinsurance.step.start

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.android.toDrawableRes
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationOverviewScreenContent
import com.hedvig.android.feature.terminateinsurance.ui.TerminationOverviewScreenScaffold
import hedvig.resources.R

@Composable
internal fun TerminationStartDestination(
  viewModel: TerminationStartStepViewModel,
  insuranceDisplayName: String,
  exposureName: String,
  contractGroup: ContractGroup,
  imageLoader: ImageLoader,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  navigateToNextStep: (TerminateInsuranceStep) -> Unit,
) {
  val uiState: TerminationStartStepUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep != null) {
      viewModel.emit(TerminationStartStepEvent.HandledNextStep)
      navigateToNextStep(nextStep)
    }
  }
  TerminationStartScreen(
    uiState = uiState,
    insuranceDisplayName = insuranceDisplayName,
    exposureName = exposureName,
    contractGroup = contractGroup,
    imageLoader = imageLoader,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
    onErrorDismissed = { viewModel.emit(TerminationStartStepEvent.HandledShowingNetworkError) },
    initiateTerminationFlow = { viewModel.emit(TerminationStartStepEvent.InitiateTerminationFlow) },
  )
}

@Composable
private fun TerminationStartScreen(
  uiState: TerminationStartStepUiState,
  insuranceDisplayName: String,
  exposureName: String,
  contractGroup: ContractGroup,
  imageLoader: ImageLoader,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  onErrorDismissed: () -> Unit,
  initiateTerminationFlow: () -> Unit,
) {
  TerminationOverviewScreenScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.TERMINATION_BUTTON),
  ) {
    if (uiState.failedToLoadNextStep) {
      ErrorDialog(
        message = stringResource(R.string.NETWORK_ERROR_ALERT_MESSAGE),
        title = stringResource(R.string.NETWORK_ERROR_ALERT_TITLE),
        onDismiss = onErrorDismissed,
      )
    }
    TerminationOverviewScreenContent(
      terminationDate = null,
      insuranceDisplayName = insuranceDisplayName,
      exposureName = exposureName,
      insuranceCardPainter = painterResource(contractGroup.toDrawableRes()),
      imageLoader = imageLoader,
      infoText = null,
      containedButtonText = stringResource(R.string.TERMINATION_BUTTON),
      onContainedButtonClick = {
        if (!uiState.isLoadingNextStep && !uiState.failedToLoadNextStep) {
          initiateTerminationFlow()
        }
      },
      isContainedButtonLoading = uiState.isLoadingNextStep,
      containedButtonColor = MaterialTheme.colorScheme.primary,
      onContainedButtonColor = MaterialTheme.colorScheme.onPrimary,
      textButtonText = stringResource(R.string.general_cancel_button),
      onTextButtonClick = navigateBack,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationStartScreenWithError() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationStartScreen(
        uiState = TerminationStartStepUiState.Initial,
        insuranceDisplayName = "insuranceDisplayName",
        exposureName = "exposureName",
        contractGroup = ContractGroup.DOG,
        imageLoader = rememberPreviewImageLoader(),
        navigateUp = {},
        navigateBack = {},
        onErrorDismissed = {},
        initiateTerminationFlow = {},
      )
    }
  }
}
