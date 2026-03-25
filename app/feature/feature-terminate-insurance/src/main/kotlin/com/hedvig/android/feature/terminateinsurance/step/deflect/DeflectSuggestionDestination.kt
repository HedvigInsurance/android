package com.hedvig.android.feature.terminateinsurance.step.deflect

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.terminateinsurance.data.SuggestionType
import com.hedvig.android.feature.terminateinsurance.data.TerminationAction
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.Res
import hedvig.resources.TERMINATION_BUTTON
import hedvig.resources.TERMINATION_FLOW_I_UNDERSTAND_TEXT
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeflectSuggestionDestination(
  description: String,
  url: String?,
  suggestionType: SuggestionType,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  openUrl: (String) -> Unit,
  onContinueTermination: () -> Unit,
) {
  DeflectSuggestionScreen(
    description = description,
    url = url,
    suggestionType = suggestionType,
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
    openUrl = openUrl,
    onContinueTermination = onContinueTermination,
  )
}

@Composable
private fun DeflectSuggestionScreen(
  description: String,
  url: String?,
  suggestionType: SuggestionType,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  openUrl: (String) -> Unit,
  onContinueTermination: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) { _ ->
    FlowHeading(
      title = description,
      description = null,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    if (url != null) {
      HedvigButton(
        text = stringResource(Res.string.general_continue_button),
        enabled = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        onClick = { openUrl(url) },
      )
      Spacer(Modifier.height(8.dp))
    }
    if (suggestionType in
      setOf(
        SuggestionType.AUTO_DECOMMISSION,
        SuggestionType.CAR_DECOMMISSION_INFO,
        SuggestionType.CAR_ALREADY_DECOMMISSION,
      )
    ) {
      HedvigButton(
        text = stringResource(Res.string.TERMINATION_BUTTON),
        enabled = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        onClick = onContinueTermination,
      )
      Spacer(Modifier.height(8.dp))
    }
    HedvigTextButton(
      text = stringResource(Res.string.TERMINATION_FLOW_I_UNDERSTAND_TEXT),
      onClick = closeTerminationFlow,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewDeflectSuggestionScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      DeflectSuggestionScreen(
        description = "Your car insurance will be automatically cancelled when the car is deregistered.",
        url = null,
        suggestionType = SuggestionType.AUTO_DECOMMISSION,
        navigateUp = {},
        closeTerminationFlow = {},
        openUrl = {},
        onContinueTermination = {},
      )
    }
  }
}
