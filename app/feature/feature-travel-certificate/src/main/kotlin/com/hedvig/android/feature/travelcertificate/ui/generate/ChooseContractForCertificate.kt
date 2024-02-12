package com.hedvig.android.feature.travelcertificate.ui.generate

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.SelectIndicationCircle
import com.hedvig.android.data.travelcertificate.ContractEligibleWithAddress

@Composable
private fun ChooseContractForCertificate(
  eligibleContracts: List<ContractEligibleWithAddress>,
  onContractChosen: (String) -> Unit,
) {
  var selectedContractId by remember {
    mutableStateOf<String?>(null)
  }
  Column {
    for (contract in eligibleContracts) {
      HedvigCard(
        onClick = { selectedContractId = contract.contractId },
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
    HedvigContainedButton(onClick = { selectedContractId?.let { onContractChosen(it) } }) {
      Text(text = stringResource(id = hedvig.resources.R.string.general_continue_button))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewChooseContractForCertificate() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChooseContractForCertificate(
        listOf(
          ContractEligibleWithAddress("Morbydalen 12", "keuwhwkjfhjkeharfj"),
          ContractEligibleWithAddress("Akerbyvagen 257", "sesjhfhakerfhlwkeija"),
        ),
        {},
      )
    }
  }
}
