package com.hedvig.android.feature.terminateinsurance.step.terminationreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedLinearProgress
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun TerminationConfirmationDestination(
  viewModel: TerminationConfirmationViewModel,
  navigateToNextStep: (TerminateInsuranceStep) -> Unit,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
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
  )
}

@Composable
private fun TerminationConfirmationScreen(uiState: OverviewUiState, onContinue: () -> Unit, navigateBack: () -> Unit) {
  val isSubmittingTerminationOrNavigatingForward = uiState.isSubmittingContractTermination || uiState.nextStep != null
  if (isSubmittingTerminationOrNavigatingForward) {
    HedvigFullScreenCenterAlignedLinearProgress(
      title = stringResource(id = R.string.TERMINATE_CONTRACT_TERMINATING_PROGRESS),
    )
  } else {
    AreYouSureScreen(
      type = uiState.terminationType,
      modifier = Modifier.fillMaxSize(),
      navigateUp = navigateBack,
      onContinue = onContinue,
    )
  }
}

@Composable
private fun AreYouSureScreen(
  type: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
  modifier: Modifier = Modifier,
  navigateUp: () -> Unit,
  onContinue: () -> Unit,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .padding(
        WindowInsets.safeDrawing
          .only(WindowInsetsSides.Horizontal)
          .asPaddingValues(),
      )
      .padding(horizontal = 16.dp),
  ) {
    Spacer(modifier = Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    Icon(
      imageVector = Icons.Hedvig.WarningFilled,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.warningElement,
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(id = R.string.GENERAL_ARE_YOU_SURE),
      textAlign = TextAlign.Center,
      style = LocalTextStyle.current.copy(
        lineBreak = LineBreak.Heading,
      ),
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(2.dp))
    val subtitle = when (type) {
      TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion ->
        stringResource(id = R.string.TERMINATION_FLOW_CONFIRMATION)

      is TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination ->
        stringResource(
          id = R.string.TERMINATION_FLOW_CONFIRMATION_SUBTITLE_TERMINATION,
          dateTimeFormatter.format(type.terminationDate.toJavaLocalDate()),
        )
    }
    Text(
      text = subtitle,
      textAlign = TextAlign.Center,
      style = LocalTextStyle.current.copy(
        lineBreak = LineBreak.Heading,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      ),
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      stringResource(id = R.string.TERMINATION_FLOW_CONFIRM_BUTTON),
      enabled = true,
      onClick = onContinue,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      stringResource(id = R.string.general_close_button),
      onClick = navigateUp,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(
      Modifier.padding(
        WindowInsets.safeDrawing
          .only(WindowInsetsSides.Bottom).asPaddingValues(),
      ),
    )
  }
}

@HedvigPreview
@Composable
private fun OverviewScreenPreviewDeletion(
  @PreviewParameter(
    com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider::class,
  ) isLoading: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationConfirmationScreen(
        uiState = OverviewUiState(
          terminationType = TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion,
          nextStep = null,
          errorMessage = null,
          isSubmittingContractTermination = isLoading,
        ),
        navigateBack = {},
        onContinue = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun OverviewScreenPreview(
  @PreviewParameter(
    com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider::class,
  ) isLoading: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationConfirmationScreen(
        uiState = OverviewUiState(
          terminationType = TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination(
            LocalDate(2024, 8, 9),
          ),
          nextStep = null,
          errorMessage = null,
          isSubmittingContractTermination = isLoading,
        ),
        navigateBack = {},
        onContinue = {},
      )
    }
  }
}
