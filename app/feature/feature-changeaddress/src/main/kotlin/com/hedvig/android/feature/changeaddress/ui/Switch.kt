package com.hedvig.android.feature.changeaddress.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.squircleMedium

@Composable
internal fun ChangeAddressSwitch(
  label: String,
  checked: Boolean,
  onClick: () -> Unit,
  onCheckedChange: (Boolean) -> Unit,
) {
  HedvigCard(
    modifier = Modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.squircleMedium)
      .clickable { onClick() },
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.weight(1f))
      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
      )
    }
  }
}
