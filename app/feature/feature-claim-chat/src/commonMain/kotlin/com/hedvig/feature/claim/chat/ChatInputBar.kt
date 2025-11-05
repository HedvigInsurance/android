package com.hedvig.feature.claim.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputBar(
  text: String,
  onTextChange: (String) -> Unit,
  onSend: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    OutlinedTextField(
      value = text,
      onValueChange = onTextChange,
      placeholder = { Text("Say something...") },
      modifier = Modifier.weight(1f)
    )
    Spacer(Modifier.width(8.dp))
    Button(
      onClick = onSend,
      enabled = text.isNotBlank()
    ) {
      Text("Send")
    }
  }
}
