package com.hedvig.android.feature.terminateinsurance.step.deflectAutoDecom

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R

@Composable
internal fun DeflectAutoDecomStepDestination(
  viewModel: DeflectAutoDecommissionStepViewModel,
  closeTerminationFlow: () -> Unit,
  navigateUp: () -> Unit,
  onContinueTermination: (step: TerminateInsuranceStep) -> Unit,
) {
  val uiState: DeflectAutoDecommissionUiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState) {
    val uiStateValue = uiState as? DeflectAutoDecommissionUiState.Success ?: return@LaunchedEffect
    if (uiStateValue.nextStep != null) {
      viewModel.emit(DeflectAutoDecommissionEvent.ClearTerminationStep)
      onContinueTermination(uiStateValue.nextStep)
    }
  }
  DeflectAutoDecomStepScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
    reload = { viewModel.emit(DeflectAutoDecommissionEvent.FetchNextStep) },
    fetchTerminationStep = { viewModel.emit(DeflectAutoDecommissionEvent.FetchNextStep) },
  )
}

@Composable
private fun DeflectAutoDecomStepScreen(
  uiState: DeflectAutoDecommissionUiState,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  reload: () -> Unit,
  fetchTerminationStep: () -> Unit,
) {
  when (uiState) {
    DeflectAutoDecommissionUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(
          onButtonClick = reload,
          modifier = Modifier.weight(1f),
        )
      }
    }

    DeflectAutoDecommissionUiState.Loading -> HedvigFullScreenCenterAlignedProgress()
    is DeflectAutoDecommissionUiState.Success -> DeflectAutoDecomStepSuccessScreen(
      isNextStepLoading = uiState.buttonLoading,
      navigateUp = navigateUp,
      closeTerminationFlow = closeTerminationFlow,
      fetchTerminationStep = fetchTerminationStep,
    )
  }
}

@Composable
private fun DeflectAutoDecomStepSuccessScreen(
  isNextStepLoading: Boolean,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  fetchTerminationStep: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) { _ ->
    FlowHeading(
      title = stringResource(id = R.string.TERMINATION_FLOW_AUTO_DECOM_TITLE),
      description = null,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    Spacer(Modifier.height(16.dp))
    HedvigText(
      stringResource(R.string.TERMINATION_FLOW_AUTO_DECOM_INFO),
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigText(
      stringResource(R.string.TERMINATION_FLOW_AUTO_DECOM_COVERED_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    HedvigText(
      stringResource(R.string.TERMINATION_FLOW_AUTO_DECOM_COVERED_INFO),
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigText(
      stringResource(R.string.TERMINATION_FLOW_AUTO_DECOM_COSTS_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    HedvigText(
      stringResource(R.string.TERMINATION_FLOW_AUTO_DECOM_COSTS_INFO),
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigNotificationCard(
      message = stringResource(R.string.TERMINATION_FLOW_AUTO_DECOM_NOTIFICATION),
      priority = NotificationDefaults.NotificationPriority.Info,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      stringResource(id = R.string.TERMINATION_FLOW_I_UNDERSTAND_TEXT),
      enabled = !isNextStepLoading,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = closeTerminationFlow,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.TERMINATION_BUTTON),
      isLoading = isNextStepLoading,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      fetchTerminationStep()
    }
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigShortMultiScreenPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreen(
  @PreviewParameter(
    DeflectAutoDecomUiStateProvider::class,
  ) uiState: DeflectAutoDecommissionUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DeflectAutoDecomStepScreen(
        uiState,
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class DeflectAutoDecomUiStateProvider :
  CollectionPreviewParameterProvider<DeflectAutoDecommissionUiState>(
    listOf(
      DeflectAutoDecommissionUiState.Success(),
      DeflectAutoDecommissionUiState.Success(buttonLoading = true),
      DeflectAutoDecommissionUiState.Loading,
      DeflectAutoDecommissionUiState.Failure,
    ),
  )
