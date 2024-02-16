package com.hedvig.android.feature.travelcertificate.ui.choose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.data.travelcertificate.ContractEligibleWithAddress
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
  var selectedContractId by remember {
    mutableStateOf<String?>(null)
  }
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
        Spacer(Modifier.height(24.dp))
        Text(
          text = stringResource(id = R.string.travel_certificate_select_contract_title),
          style = MaterialTheme.typography.headlineMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        for (contract in uiState.eligibleContracts) {
          HedvigCard(
            onClick = { selectedContractId = contract.contractId },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .heightIn(72.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
              Text(
                text = contract.address,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
              )
              Spacer(Modifier.width(8.dp))
              SelectIndicationCircle(selectedContractId == contract.contractId)
            }
          }
          Spacer(modifier = (Modifier.height(4.dp)))
        }
        Spacer(modifier = Modifier.height(12.dp))
        HedvigContainedButton(
          onClick = { selectedContractId?.let { onContinue(it) } },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        ) {
          Text(text = stringResource(id = R.string.general_continue_button),
            style = MaterialTheme.typography.bodyLarge
            )
        }
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
    HedvigErrorSection(retry = reload, modifier = Modifier.weight(1f))
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseContractForCertificate() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChooseContractForCertificate(
        ChooseContractUiState.Success(
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
