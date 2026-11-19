package com.hedvig.android.feature.greencard.ui.choose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.greencard.data.GreenCardContractSpecification
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChooseContractForGreenCardDestination(
  viewModel: ChooseContractForGreenCardViewModel,
  navigateUp: () -> Unit,
  onContinue: (contractId: String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChooseContractForGreenCardScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onContinue = onContinue,
    reload = { viewModel.emit(ChooseContractForGreenCardEvent.RetryLoadData) },
  )
}

@Composable
private fun ChooseContractForGreenCardScreen(
  uiState: ChooseContractForGreenCardUiState,
  navigateUp: () -> Unit,
  onContinue: (contractId: String) -> Unit,
  reload: () -> Unit,
) {
  when (uiState) {
    ChooseContractForGreenCardUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    ChooseContractForGreenCardUiState.Failure -> {
      HedvigScaffold(navigateUp = navigateUp) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    is ChooseContractForGreenCardUiState.Success -> {
      var selectedContractId by rememberSaveable { mutableStateOf<String?>(null) }
      HedvigScaffold(navigateUp = navigateUp) {
        Spacer(Modifier.height(8.dp))
        FlowHeading(
          // TODO: Add "Which vehicle is the green card for?" / "Vilket fordon gäller det gröna kortet?" to Lokalise
          "Which vehicle is the green card for?",
          null,
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        RadioGroup(
          options = uiState.contracts.map {
            RadioOption(id = RadioOptionId(it.contractId), text = it.registrationNumber)
          },
          selectedOption = selectedContractId?.let { RadioOptionId(it) },
          onRadioOptionSelected = { selectedContractId = it.id },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(Res.string.general_continue_button),
          onClick = dropUnlessResumed { selectedContractId?.let(onContinue) },
          enabled = selectedContractId != null,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseContractForGreenCard() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ChooseContractForGreenCardScreen(
        uiState = ChooseContractForGreenCardUiState.Success(
          listOf(
            GreenCardContractSpecification("a", "ABC 123", LocalDate(2026, 11, 18), LocalDate(2027, 2, 18), 90),
            GreenCardContractSpecification("b", "XYZ 789", LocalDate(2026, 11, 18), LocalDate(2027, 2, 18), 90),
          ),
        ),
        navigateUp = {},
        onContinue = {},
        reload = {},
      )
    }
  }
}
