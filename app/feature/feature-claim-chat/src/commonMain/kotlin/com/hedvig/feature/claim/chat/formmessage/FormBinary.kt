package com.hedvig.feature.claim.chat.formmessage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FormBinary(
  value: String?,
  id: String,
  title: String?,
  defaultValue: String?,
  options: List<String>,
  onOptionSelected: (String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  FormContainer(modifier = modifier) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Button(
        onClick = { onOptionSelected(options.getOrNull(0)) },
        modifier = Modifier.weight(1f),
      ) {
        Text(options.getOrNull(0) ?: "No option found")
      }
      Spacer(Modifier.width(4.dp))
      OutlinedButton(
        onClick = { onOptionSelected(options.getOrNull(1)) },
        modifier = Modifier.weight(1f),
      ) {
        Text(options.getOrNull(1) ?: "No option found")
      }
    }
  }
}
