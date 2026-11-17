package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.pillowResource
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.editcoinsured.data.ContractInfo
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun InsuranceInfoCard(contractInfo: ContractInfo, modifier: Modifier = Modifier) {
  HedvigCard(
    modifier = modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        color = HedvigTheme.colorScheme.borderPrimary,
        shape = HedvigTheme.shapes.cornerXLarge,
      ),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(16.dp),
    ) {
      Image(
        painter = painterResource(contractInfo.contractGroup.pillowResource()),
        contentDescription = null,
        modifier = Modifier.size(48.dp),
      )
      Spacer(Modifier.width(12.dp))
      Column(Modifier.weight(1f)) {
        HedvigText(contractInfo.displayName)
        HedvigText(contractInfo.exposureName, color = HedvigTheme.colorScheme.textSecondary)
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceInfoCard() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InsuranceInfoCard(
        contractInfo = ContractInfo(
          contractGroup = ContractGroup.RENTAL,
          displayName = "Home insurance",
          exposureName = "Lulanden 85H, 71220",
        ),
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}
