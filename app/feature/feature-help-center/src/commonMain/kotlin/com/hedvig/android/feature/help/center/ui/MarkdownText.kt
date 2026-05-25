package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Renders Markdown content.
 * On Android: Uses RichText library for full Markdown rendering
 * On iOS/other platforms: Uses com.mikepenz.markdown lib
 */
@Composable
expect fun MarkdownText(markdown: String, modifier: Modifier = Modifier, withArticleStyle: Boolean = false)

fun Int.toHapticFeedbackType(): HapticFeedbackType {
  return when (this) {
    1, 2 -> HapticFeedbackType.TextHandleMove
    4,5 -> HapticFeedbackType.Confirm
    else -> HapticFeedbackType.LongPress
  }
}
