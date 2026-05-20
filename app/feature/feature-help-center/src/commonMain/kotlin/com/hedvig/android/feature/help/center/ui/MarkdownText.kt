package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders Markdown content.
 * On Android: Uses RichText library for full Markdown rendering
 * On iOS/other platforms: Uses com.mikepenz.markdown lib
 */
@Composable
expect fun MarkdownText(markdown: String, modifier: Modifier = Modifier, withArticleStyle: Boolean = false)

@Composable
expect fun PerformHapticFeedback(intensity: HapticIntensity)

enum class HapticIntensity {
  LIGHT,
  MEDIUM,
  SUCCESS
}

fun Int.toHapticIntensity(): HapticIntensity {
  return when (this) {
    1, 2 -> HapticIntensity.LIGHT
    4,5 -> HapticIntensity.SUCCESS
    else -> HapticIntensity.MEDIUM
  }
}
