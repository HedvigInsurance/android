package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography

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
      override val codeText: Color = colors.textPrimary
      override val inlineCodeText: Color = colors.textPrimary
      override val linkText: Color = colors.link
      override val codeBackground: Color = colors.surfaceSecondary
      override val inlineCodeBackground: Color = colors.surfaceSecondary
      override val dividerColor: Color = colors.borderPrimary
      override val tableBackground: Color = colors.surfaceSecondary
      override val tableText: Color = colors.textPrimary
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
      override val link: TextStyle = typography.bodySmall
    },
  )
}
