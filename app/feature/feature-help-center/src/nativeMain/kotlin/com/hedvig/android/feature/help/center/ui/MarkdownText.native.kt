package com.hedvig.android.feature.help.center.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.ProvideTextStyle
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography

@Composable
actual fun MarkdownText(markdown: String, modifier: Modifier, withArticleStyle: Boolean) {
  val colors = HedvigTheme.colorScheme
  val typography = HedvigTheme.typography

  Markdown(
    content = markdown,
    modifier = modifier,
    colors = object : MarkdownColors {
      override val text: Color = colors.textSecondaryTranslucent
      override val codeBackground: Color = colors.surfaceSecondary
      override val inlineCodeBackground: Color = colors.surfaceSecondary
      override val dividerColor: Color = colors.borderPrimary
      override val tableBackground: Color = colors.surfaceSecondary
    },
    typography = object : MarkdownTypography {
      override val h1: TextStyle = typography.headlineLarge.copy(
        color = HedvigTheme.colorScheme.textPrimary,
        fontWeight = FontWeight.Normal)
      override val h2: TextStyle = typography.headlineMedium.copy(
        color = HedvigTheme.colorScheme.textPrimary,
        fontWeight = FontWeight.Normal)
      override val h3: TextStyle = typography.headlineSmall.copy(
        color = HedvigTheme.colorScheme.textPrimary,
        fontWeight = FontWeight.Normal)
      override val h4: TextStyle = typography.displaySmall.copy(
        color = HedvigTheme.colorScheme.textPrimary,
        fontWeight = FontWeight.Normal)
      override val h5: TextStyle = typography.bodyLarge.copy(
        color = HedvigTheme.colorScheme.textPrimary,
        fontWeight = FontWeight.Normal)
      override val h6: TextStyle = typography.bodyMedium.copy(
        color = HedvigTheme.colorScheme.textPrimary,
        fontWeight = FontWeight.Normal)
      override val text: TextStyle = typography.bodySmall
      override val paragraph: TextStyle = typography.bodySmall.copy(
        color = HedvigTheme.colorScheme.textPrimary,
        fontWeight = FontWeight.Normal)
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
