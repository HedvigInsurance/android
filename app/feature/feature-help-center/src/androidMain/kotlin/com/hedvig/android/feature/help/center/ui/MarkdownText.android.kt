package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import com.halilibo.richtext.commonmark.Markdown
import  com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.string.RichTextStringStyle
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RichText

@Composable
actual fun MarkdownText(
  markdown: String,
  modifier: Modifier,
  withArticleStyle: Boolean) {
  val headingColor = HedvigTheme.colorScheme.textPrimary
  val style = if (withArticleStyle) RichTextStyle(
    headingStyle = { _, currentStyle ->
      currentStyle.copy(
        color = headingColor,
      )
    },
    stringStyle = RichTextStringStyle(
      boldStyle = SpanStyle(
        color = headingColor,
      ),
    ),
  ) else null
  RichText(modifier = modifier,
    style = style) {
    Markdown(content = markdown)
  }
}
