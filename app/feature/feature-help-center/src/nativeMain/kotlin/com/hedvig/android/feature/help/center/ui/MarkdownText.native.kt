package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigText

@Composable
actual fun MarkdownText(markdown: String,
                        modifier: Modifier,
                        withArticleStyle: Boolean) {
  // Fallback to plain text for native platforms
  // TODO ios: Find a KMP markdown library in the future
  HedvigText(
    text = markdown,
    modifier = modifier,
  )
}
