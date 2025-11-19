package com.hedvig.feature.claim.chat.assistantmessage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AssistantChatMessage(text: String, subText: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth().padding(16.dp),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
      fontSize = 24.sp,
    )
    if (subText.isNotBlank()) {
      Text(
        text = subText,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 24.sp,
      )
    }

    AssistantLabel(modifier)
  }
}
