package com.hedvig.feature.claim.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
fun AssistantChatMessage(
  text: String,
  subText: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(16.dp),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
      fontSize = 24.sp,
    )
    Text(
      text = subText,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.secondary,
      fontSize = 24.sp,
    )

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
}


