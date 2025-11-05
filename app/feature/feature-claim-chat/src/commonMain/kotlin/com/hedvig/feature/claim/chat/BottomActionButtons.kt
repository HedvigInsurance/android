package com.hedvig.feature.claim.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomActionButtons(
  onDontHaveReceipt: () -> Unit,
  onScanReceipt: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    TextButton(
      onClick = onDontHaveReceipt,
      modifier = Modifier.weight(1f)
    ) {
      Text("I don't have it")
    }
    FilledTonalButton(
      onClick = onScanReceipt,
      modifier = Modifier.weight(1f)
    ) {
      Text("Scan receipt")
    }
  }
}
