package com.hedvig.android.feature.help.center.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun MarkdownText(markdown: String, modifier: Modifier) {
  // Fallback to plain text for native platforms
  // TODO ios: Find a KMP markdown library in the future
  Text(
    text = markdown,
    modifier = modifier,
  )
}
