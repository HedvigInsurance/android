package com.hedvig.android.feature.terminateinsurance.step.deflectAutoCancel

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.R

@Composable
internal fun DeflectAutoCancelStepDestination(
  viewModel: DeflectAutoCancelStepViewModel,
  onNavigateToNewConversation: () -> Unit,
  closeTerminationFlow: () -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState: DeflectAutoCancelUiState by viewModel.uiState.collectAsStateWithLifecycle()
  when (val state = uiState) {
    is DeflectAutoCancelUiState.Success -> DeflectAutoCancelScreen(
      state,
      onNavigateToNewConversation = onNavigateToNewConversation,
      closeTerminationFlow = closeTerminationFlow,
      navigateUp = navigateUp,
    )
  }

}

@Composable
private fun DeflectAutoCancelScreen(
  uiState: DeflectAutoCancelUiState.Success,
  onNavigateToNewConversation: () -> Unit,
  closeTerminationFlow: () -> Unit,
  navigateUp: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) { _ ->
    FlowHeading(
      title = stringResource(id = R.string.TERMINATION_FLOW_AUTO_CANCEL_TITLE),
      description = null,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    Spacer(Modifier.height(16.dp))
    HedvigText(uiState.message,
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(16.dp))
    HedvigText(stringResource(id = R.string.TERMINATION_FLOW_AUTO_CANCEL_ABOUT),
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      stringResource(id = R.string.TERMINATION_FLOW_I_UNDERSTAND_TEXT),
      enabled = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = closeTerminationFlow,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(text = stringResource(R.string.DASHBOARD_OPEN_CHAT),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),) {
      onNavigateToNewConversation()
    }
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigShortMultiScreenPreview
@Composable
private fun PreviewChooseInsuranceToTerminateScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DeflectAutoCancelScreen(
        DeflectAutoCancelUiState.Success(
          "If you’ve scrapped your car, we’ll cancel your " +
            "insurance automatically.",
        ),
        {},
        {},
        {},
      )
    }
  }
}
