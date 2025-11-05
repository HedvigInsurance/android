package com.hedvig.feature.claim.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AssistantLoadingState(
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
      fontSize = 14.sp,
    )
    Spacer(Modifier.padding(top = 6.dp))
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ThreeDotLoadingIndicator()
    }
  }
}


