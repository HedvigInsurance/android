package com.hedvig.android.feature.travelcertificate.ui.choose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ChosenState.Chosen
import com.hedvig.android.design.system.hedvig.ChosenState.NotChosen
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOptionData
import com.hedvig.android.design.system.hedvig.RadioOptionGroupData.RadioOptionGroupDataSimple
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.feature.travelcertificate.data.ContractEligibleWithAddress
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractUiState.Success
import hedvig.resources.R

@Composable
internal fun ChooseContractForCertificateDestination(
  viewModel: ChooseContractForCertificateViewModel,
  navigateUp: () -> Unit,
  onContinue: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChooseContractForCertificate(
    uiState = uiState,
    navigateUp = navigateUp,
    onContinue = onContinue,
    reload = { viewModel.emit(ChooseContractEvent.RetryLoadData) },
  )
}

@Composable
private fun ChooseContractForCertificate(
  uiState: ChooseContractUiState,
  navigateUp: () -> Unit,
  onContinue: (String) -> Unit,
  reload: () -> Unit,
) {
  var selectedContractId by remember { mutableStateOf<String?>(null) }
  when (uiState) {
    ChooseContractUiState.Failure -> {
      FailureScreen(
        navigateUp = navigateUp,
        reload = reload,
      )
    }

    ChooseContractUiState.Loading -> {
      HedvigFullScreenCenterAlignedProgress()
    }

    is ChooseContractUiState.Success -> {
      HedvigScaffold(
        navigateUp = navigateUp,
        modifier = Modifier.clearFocusOnTap(),
      ) {
        Spacer(Modifier.height(8.dp))
        HedvigText(
          text = stringResource(R.string.travel_certificate_select_contract_title),
          style = HedvigTheme.typography.headlineMedium.copy(
            lineBreak = LineBreak.Heading,
          ),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        RadioGroup(
          radioGroupStyle = RadioGroupStyle.Vertical.Default(
            uiState.eligibleContracts.map {
              RadioOptionGroupDataSimple(
                radioOptionData = RadioOptionData(
                  id = it.contractId,
                  optionText = it.address,
                  chosenState = if (it.contractId == selectedContractId) Chosen else NotChosen,
                ),
              )
            },
          ),
          onOptionClick = { contractId ->
            selectedContractId = contractId
          },
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(id = R.string.general_continue_button),
          onClick = { selectedContractId?.let { onContinue(it) } },
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

@Composable
private fun FailureScreen(navigateUp: () -> Unit, reload: () -> Unit) {
  HedvigScaffold(
    navigateUp = navigateUp,
  ) {
    HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseContractForCertificate() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ChooseContractForCertificate(
        Success(
          listOf(
            ContractEligibleWithAddress("Morbydalen 12", "keuwhwkjfhjkeharfj"),
            ContractEligibleWithAddress("Akerbyvagen 257", "sesjhfhakerfhlwkeija"),
          ),
        ),
        {},
        {},
        {},
      )
    }
  }
}
