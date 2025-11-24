package com.hedvig.android.feature.terminateinsurance.step.deflectAutoCancel

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.terminateinsurance.navigation.AutoCancelDeflectStepParameters
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.Res
import hedvig.resources.DASHBOARD_OPEN_CHAT
import hedvig.resources.TERMINATION_FLOW_I_UNDERSTAND_TEXT
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeflectAutoCancelStepDestination(
  params: AutoCancelDeflectStepParameters,
  onNavigateToNewConversation: () -> Unit,
  closeTerminationFlow: () -> Unit,
  navigateUp: () -> Unit,
) {
  DeflectAutoCancelScreen(
    params,
    onNavigateToNewConversation = onNavigateToNewConversation,
    closeTerminationFlow = closeTerminationFlow,
    navigateUp = navigateUp,
  )
}

@Composable
private fun DeflectAutoCancelScreen(
  params: AutoCancelDeflectStepParameters,
  onNavigateToNewConversation: () -> Unit,
  closeTerminationFlow: () -> Unit,
  navigateUp: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) { _ ->
    FlowHeading(
      title = params.title,
      description = null,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    Spacer(Modifier.height(16.dp))
    HedvigText(
      params.message,
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    if (params.extraMessage != null) {
      Spacer(Modifier.height(16.dp))
      HedvigText(
        params.extraMessage,
        color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      stringResource(Res.string.TERMINATION_FLOW_I_UNDERSTAND_TEXT),
      enabled = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      onClick = closeTerminationFlow,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.DASHBOARD_OPEN_CHAT),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
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
        AutoCancelDeflectStepParameters(
          title = "We’ll cancel your insurance automatically",
          message = "If you’ve scrapped your car, we’ll cancel your " +
            "insurance automatically.",
          extraMessage = "We’ll send a cancellation confirmation within a few days. If you don’t get it after " +
            "5 days, feel free to contact us.",
        ),
        {},
        {},
        {},
      )
    }
  }
}
