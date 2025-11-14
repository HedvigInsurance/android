package com.hedvig.feature.claim.chat.assistantmessage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AssistantLabel(modifier: Modifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(Color(0xFF34C759)),
    )
    Spacer(Modifier.padding(start = 4.dp))
    Text(
      text = "Hedvig AI Assistant",
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.secondary,
      fontSize = 11.sp,
    )
  }
}
