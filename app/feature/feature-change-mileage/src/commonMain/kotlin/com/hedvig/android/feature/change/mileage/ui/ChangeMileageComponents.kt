package com.hedvig.android.feature.change.mileage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.pillowResource
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun MileageCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Surface(
    modifier = modifier
      .shadow(elevation = 2.dp, shape = HedvigTheme.shapes.cornerXLarge)
      .border(
        shape = HedvigTheme.shapes.cornerXLarge,
        color = HedvigTheme.colorScheme.borderPrimary,
        width = 1.dp,
      ),
    shape = HedvigTheme.shapes.cornerXLarge,
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(Modifier.padding(16.dp), content = content)
  }
}

@Composable
internal fun ContractHeader(
  contractGroup: ContractGroup,
  displayName: String,
  subtitle: String,
  modifier: Modifier = Modifier,
) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
    Image(
      painter = painterResource(contractGroup.pillowResource()),
      contentDescription = null,
      modifier = Modifier.size(48.dp),
    )
    Spacer(Modifier.width(16.dp))
    Column {
      HedvigText(
        text = displayName,
        style = HedvigTheme.typography.headlineSmall,
      )
      HedvigText(
        text = subtitle,
        color = HedvigTheme.colorScheme.textSecondary,
        style = HedvigTheme.typography.bodySmall,
      )
    }
  }
}
