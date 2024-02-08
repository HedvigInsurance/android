package com.hedvig.android.feature.terminateinsurance.step.terminationreview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.android.toDrawableRes
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationOverviewScreenContent
import com.hedvig.android.feature.terminateinsurance.ui.TerminationOverviewScreenScaffold
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun TerminationReviewDestination(
  viewModel: TerminationReviewViewModel,
  imageLoader: ImageLoader,
  navigateToNextStep: (TerminateInsuranceStep) -> Unit,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep == null) return@LaunchedEffect
    navigateToNextStep(nextStep)
  }

  TerminationReviewScreen(
    uiState = uiState,
    onContinue = onContinue,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
    imageLoader = imageLoader,
  )
}

@Composable
private fun TerminationReviewScreen(
  uiState: OverviewUiState,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  imageLoader: ImageLoader,
) {
  val isSubmittingTerminationOrNavigatingForward = uiState.isSubmittingContractTermination || uiState.nextStep != null
  if (isSubmittingTerminationOrNavigatingForward) {
    HedvigFullScreenCenterAlignedLinearProgress(
      title = stringResource(id = R.string.TERMINATE_CONTRACT_TERMINATING_PROGRESS),
    )
  } else {
    TerminationOverviewScreenScaffold(
      navigateUp = navigateUp,
      topAppBarText = stringResource(R.string.TERMINATION_CONFIRM_BUTTON),
    ) {
      TerminationOverviewScreenContent(
        terminationDate = uiState.terminationDate,
        insuranceDisplayName = uiState.insuranceDisplayName,
        exposureName = uiState.exposureName,
        insuranceCardPainter = painterResource(uiState.contractGroup.toDrawableRes()),
        imageLoader = imageLoader,
        infoText = null,
        containedButtonText = stringResource(R.string.TERMINATION_CONFIRM_BUTTON),
        onContainedButtonClick = onContinue,
        textButtonText = stringResource(R.string.general_cancel_button),
        onTextButtonClick = navigateBack,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun OverviewScreenPreview(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) isLoading: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationReviewScreen(
        uiState = OverviewUiState(
          terminationDate = LocalDate.fromEpochDays(300),
          insuranceDisplayName = "Test insurance",
          exposureName = "destination.exposureName",
          contractGroup = ContractGroup.CAR,
          nextStep = null,
          errorMessage = null,
          isSubmittingContractTermination = isLoading,
        ),
        imageLoader = rememberPreviewImageLoader(),
        navigateUp = {},
        navigateBack = {},
        onContinue = {},
      )
    }
  }
}
