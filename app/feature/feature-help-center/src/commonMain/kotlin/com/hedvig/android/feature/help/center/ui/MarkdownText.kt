package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders Markdown content.
 * On Android: Uses RichText library for full Markdown rendering
 * On iOS/other platforms: Falls back to plain text
 */
@Composable
expect fun MarkdownText(markdown: String, modifier: Modifier = Modifier)
