package com.hedvig.android.feature.terminateinsurance.step.chooseaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.ERROR
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TerminationChooseActionDestination(
  viewModel: TerminationChooseActionViewModel,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TerminationChooseActionScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
    selectAction = { viewModel.emit(TerminationChooseActionEvent.SelectAction(it)) },
    onContinue = { viewModel.emit(TerminationChooseActionEvent.Continue) },
  )
}

@Composable
private fun TerminationChooseActionScreen(
  uiState: TerminationChooseActionUiState,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  selectAction: (TerminationChooseAction) -> Unit,
  onContinue: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) {
    FlowHeading(
      // TODO: Add "Cancel insurance" / "Avsluta försäkringen" to Lokalise
      "Avsluta försäkringen",
      // TODO: Add "What would you like to do?" / "Vad vill du göra?" to Lokalise
      "Vad vill du göra?",
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    AnimatedVisibility(
      visible = uiState.navigationStepFailedToLoad,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Column {
        EmptyState(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .wrapContentWidth(),
          text = stringResource(Res.string.something_went_wrong),
          iconStyle = ERROR,
          description = null,
        )
        Spacer(Modifier.height(16.dp))
      }
    }
    RadioGroup(
      options = listOf(
        RadioOption(
          id = RadioOptionId(TerminationChooseAction.Withdraw.name),
          // TODO: Add "Withdraw insurance" / "Ångra försäkringen" to Lokalise
          text = "Ångra försäkringen",
          // TODO: Add "The insurance ends today. You only pay for the days you have been insured." /
          //  "Försäkringen avslutas idag. Du betalar bara för dagarna du har varit försäkrad." to Lokalise
          label = "Försäkringen avslutas idag. Du betalar bara för dagarna du har varit försäkrad.",
        ),
        RadioOption(
          id = RadioOptionId(TerminationChooseAction.Terminate.name),
          // TODO: Add "Cancel insurance" / "Säg upp försäkringen" to Lokalise
          text = "Säg upp försäkringen",
          // TODO: Add "The insurance is valid until the date you choose." /
          //  "Försäkringen gäller tills det datum du väljer." to Lokalise
          label = "Försäkringen gäller tills det datum du väljer.",
        ),
      ),
      selectedOption = uiState.selectedAction?.let { RadioOptionId(it.name) },
      onRadioOptionSelected = { selectAction(TerminationChooseAction.valueOf(it.id)) },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      enabled = uiState.selectedAction != null,
      onClick = onContinue,
      isLoading = uiState.isNavigationStepLoading,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationChooseActionScreen(
  @PreviewParameter(TerminationChooseActionUiStateProvider::class) uiState: TerminationChooseActionUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationChooseActionScreen(
        uiState = uiState,
        navigateUp = {},
        closeTerminationFlow = {},
        selectAction = {},
        onContinue = {},
      )
    }
  }
}

private class TerminationChooseActionUiStateProvider :
  CollectionPreviewParameterProvider<TerminationChooseActionUiState>(
    listOf(
      TerminationChooseActionUiState(
        selectedAction = null,
        isNavigationStepLoading = false,
        navigationStepFailedToLoad = false,
      ),
      TerminationChooseActionUiState(
        selectedAction = TerminationChooseAction.Terminate,
        isNavigationStepLoading = true,
        navigationStepFailedToLoad = true,
      ),
    ),
  )
