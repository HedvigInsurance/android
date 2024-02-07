package com.hedvig.android.feature.terminateinsurance.step.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.android.toDrawableRes
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationSummary
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun OverviewDestination(
  viewModel: OverviewViewModel,
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

  OverViewScreen(
    uiState = uiState,
    onContinue = onContinue,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
    imageLoader = imageLoader,
  )
}

@Composable
private fun OverViewScreen(
  uiState: OverviewUiState,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  imageLoader: ImageLoader,
) {
  val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Column(
    Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
  ) {
    val isSubmittingTerminationOrNavigatingForward = uiState.isSubmittingContractTermination || uiState.nextStep != null
    if (isSubmittingTerminationOrNavigatingForward) {
      HedvigFullScreenCenterAlignedLinearProgress(
        title = stringResource(id = R.string.TERMINATE_CONTRACT_TERMINATING_PROGRESS),
      )
    } else {
      HedvigScaffold(
        navigateUp = navigateUp,
        topAppBarScrollBehavior = topAppBarScrollBehavior,
        topAppBarText = stringResource(R.string.TERMINATION_CONFIRM_BUTTON),
      ) {
        Spacer(Modifier.height(8.dp))
        TerminationSummary(
          selectedDate = uiState.terminationDate,
          insuranceDisplayName = uiState.insuranceDisplayName,
          exposureName = uiState.exposureName,
          insuranceCardPainter = painterResource(uiState.contractGroup.toDrawableRes()),
          imageLoader = imageLoader,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(32.dp))
        Spacer(Modifier.weight(1f))
        HedvigContainedButton(
          text = stringResource(id = R.string.TERMINATION_CONFIRM_BUTTON),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
          ),
          onClick = onContinue,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(id = R.string.general_cancel_button),
          onClick = navigateBack,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
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
      OverViewScreen(
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
