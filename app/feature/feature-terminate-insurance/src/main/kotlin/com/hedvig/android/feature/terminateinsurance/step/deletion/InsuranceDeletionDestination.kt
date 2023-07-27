package com.hedvig.android.feature.terminateinsurance.step.deletion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.snackbar.ErrorSnackbar
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoScreen
import hedvig.resources.R

@Composable
internal fun InsuranceDeletionDestination(
  viewModel: InsuranceDeletionViewModel,
  insuranceDisplayName: String,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (TerminateInsuranceStep) -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep == null) return@LaunchedEffect
    navigateToNextStep(nextStep)
  }
  InsuranceDeletionScreen(
    uiState = uiState,
    insuranceDisplayName = insuranceDisplayName,
    windowSizeClass = windowSizeClass,
    showedError = viewModel::showedError,
    confirmDeletion = viewModel::confirmDeletion,
    navigateBack = navigateBack,
  )
}

@Composable
private fun InsuranceDeletionScreen(
  uiState: InsuranceDeletionUiState,
  insuranceDisplayName: String,
  windowSizeClass: WindowSizeClass,
  showedError: () -> Unit,
  confirmDeletion: () -> Unit,
  navigateBack: () -> Unit,
) {
  Box {
    TerminationInfoScreen(
      windowSizeClass = windowSizeClass,
      title = "",
      headerText = stringResource(
        R.string.TERMINATION_CONTRACT_DELETION_ALERT_DESCRIPTION,
        insuranceDisplayName,
      ),
      bodyText = uiState.disclaimer,
      icon = ImageVector.vectorResource(com.hedvig.android.core.design.system.R.drawable.ic_warning_triangle),
      navigateUp = navigateBack,
    ) {
      Column {
        LargeOutlinedTextButton(
          text = stringResource(R.string.general_cancel_button),
          onClick = navigateBack,
          enabled = uiState.canSubmit,
        )
        Spacer(Modifier.height(16.dp))
        LargeContainedTextButton(
          text = stringResource(R.string.general_continue_button),
          onClick = { confirmDeletion() },
          enabled = uiState.canSubmit,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            disabledContainerColor = MaterialTheme.colorScheme.error.copy(
              alpha = 0.12f,
            ),
            disabledContentColor = MaterialTheme.colorScheme.onError.copy(
              alpha = 0.38f,
            ),
          ),
        )
      }
    }
    ErrorSnackbar(
      hasError = uiState.hasError,
      showedError = showedError,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .windowInsetsPadding(WindowInsets.safeDrawing),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceDeletionScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      InsuranceDeletionScreen(
        InsuranceDeletionUiState(
          "Your insurance will be deleted which means it will not be activated on 2023-01-23",
        ),
        "Home Insurance",
        WindowSizeClass.calculateForPreview(),
        {},
        {},
        {},
      )
    }
  }
}
