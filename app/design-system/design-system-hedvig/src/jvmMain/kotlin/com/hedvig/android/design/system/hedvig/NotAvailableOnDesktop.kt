package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NotAvailableOnDesktop(modifier: Modifier = Modifier) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier.fillMaxSize().padding(16.dp),
  ) {
    HedvigText(
      text = "Not available on desktop",
      style = HedvigTheme.typography.bodySmall,
      color = HedvigTheme.colorScheme.textSecondary,
      textAlign = TextAlign.Center,
    )
  }
}
