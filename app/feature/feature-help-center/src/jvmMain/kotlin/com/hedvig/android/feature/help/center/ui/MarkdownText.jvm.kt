package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.halilibo.richtext.commonmark.Markdown
import com.hedvig.android.design.system.hedvig.RichText

@Composable
actual fun MarkdownText(markdown: String, modifier: Modifier) {
  RichText(modifier = modifier) {
    Markdown(content = markdown)
  }
}
