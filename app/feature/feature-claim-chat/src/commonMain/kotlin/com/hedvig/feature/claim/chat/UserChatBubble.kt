package com.hedvig.feature.claim.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserChatBubble(
  text: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End
  ) {
    Box(
      modifier = Modifier
        .widthIn(min = 50.dp, max = 300.dp)
        .background(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
          shape = RoundedCornerShape(16.dp)
        )
        .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}
