package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.*

@Composable
actual fun MarkdownText(
  markdown: String,
  modifier: Modifier,
  withArticleStyle: Boolean,
) {
  val colors = HedvigTheme.colorScheme
  val typography = HedvigTheme.typography

  Markdown(
    content = markdown,
    modifier = modifier,
    colors = object : MarkdownColors {
      override val text: Color = colors.textPrimary
      override val codeBackground: Color = colors.surfaceSecondary
      override val inlineCodeBackground: Color = colors.surfaceSecondary
      override val dividerColor: Color = colors.borderPrimary
      override val tableBackground: Color = colors.surfaceSecondary
    },
    typography = object : MarkdownTypography {
      override val h1: TextStyle = typography.headlineLarge
      override val h2: TextStyle = typography.headlineMedium
      override val h3: TextStyle = typography.headlineSmall
      override val h4: TextStyle = typography.displaySmall
      override val h5: TextStyle = typography.bodyLarge
      override val h6: TextStyle = typography.bodyMedium
      override val text: TextStyle = typography.bodySmall
      override val paragraph: TextStyle = typography.bodySmall
      override val code: TextStyle = typography.label
      override val inlineCode: TextStyle = typography.label
      override val bullet: TextStyle = typography.bodySmall
      override val list: TextStyle = typography.bodySmall
      override val ordered: TextStyle = typography.bodySmall
      override val quote: TextStyle = typography.bodySmall
      override val table: TextStyle = typography.bodySmall
      override val textLink = TextLinkStyles(
        style = typography.bodySmall.copy(color = colors.link).toSpanStyle(),
        focusedStyle = typography.bodySmall.copy(color = colors.link).toSpanStyle(),
        hoveredStyle = typography.bodySmall.copy(color = colors.link).toSpanStyle(),
        pressedStyle = typography.bodySmall.copy(color = colors.link).toSpanStyle(),
      )
    },
  )
}
