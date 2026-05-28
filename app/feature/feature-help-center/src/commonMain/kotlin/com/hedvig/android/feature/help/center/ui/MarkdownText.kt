package com.hedvig.android.feature.help.center.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownPadding
import com.mikepenz.markdown.model.MarkdownTypography

@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier, withArticleStyle: Boolean = false) {
  val colors = HedvigTheme.colorScheme
  val typography = HedvigTheme.typography
  val headingColor = colors.textPrimary

  val markdownColors = object : MarkdownColors {
    override val text: Color = colors.textSecondaryTranslucent
    override val codeBackground: Color = colors.surfaceSecondary
    override val inlineCodeBackground: Color = colors.surfaceSecondary
    override val dividerColor: Color = colors.borderPrimary
    override val tableBackground: Color = colors.surfaceSecondary
  }

  val markdownTypography = if (withArticleStyle) {
    object : MarkdownTypography {
      override val h1: TextStyle = typography.bodySmall.copy(
        color = headingColor,
        fontWeight = FontWeight.Normal,
      )
      override val h2: TextStyle = typography.bodySmall.copy(
        color = headingColor,
        fontWeight = FontWeight.Normal,
      )
      override val h3: TextStyle = typography.bodySmall.copy(
        color = headingColor,
        fontWeight = FontWeight.Normal,
      )
      override val h4: TextStyle = typography.bodySmall.copy(
        color = headingColor,
        fontWeight = FontWeight.Normal,
      )
      override val h5: TextStyle = typography.bodySmall.copy(
        color = headingColor,
        fontWeight = FontWeight.Normal,
      )
      override val h6: TextStyle = typography.bodySmall.copy(
        color = headingColor,
        fontWeight = FontWeight.Normal,
      )
      override val text: TextStyle = typography.bodySmall.copy(
        color = colors.textSecondaryTranslucent,
        fontWeight = FontWeight.Normal,
      )
      override val paragraph: TextStyle = typography.bodySmall.copy(
        color = colors.textSecondaryTranslucent,
        fontWeight = FontWeight.Normal,
      )
      override val code: TextStyle = typography.label
      override val inlineCode: TextStyle = typography.label
      override val bullet: TextStyle = typography.bodySmall.copy(
        color = colors.textSecondaryTranslucent,
      )
      override val list: TextStyle = typography.bodySmall.copy(
        color = colors.textSecondaryTranslucent,
      )
      override val ordered: TextStyle = typography.bodySmall.copy(
        color = colors.textSecondaryTranslucent,
      )
      override val quote: TextStyle = typography.bodySmall.copy(
        color = colors.textSecondaryTranslucent,
      )
      override val table: TextStyle = typography.bodySmall.copy(
        color = colors.textSecondaryTranslucent,
      )
      override val textLink = TextLinkStyles(
        style = SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline),
        focusedStyle = SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline),
        hoveredStyle = SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline),
        pressedStyle = SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline),
      )
    }
  } else {
    object : MarkdownTypography {
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
        style = SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline),
        focusedStyle = SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline),
        hoveredStyle = SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline),
        pressedStyle = SpanStyle(color = colors.link, textDecoration = TextDecoration.Underline),
      )
    }
  }

  Markdown(
    content = markdown,
    modifier = modifier,
    colors = markdownColors,
    typography = markdownTypography,
    padding = object : MarkdownPadding {
      override val block: Dp = 6.dp
      override val blockQuote: PaddingValues = PaddingValues(0.dp)
      override val blockQuoteBar: PaddingValues.Absolute = PaddingValues.Absolute(0.dp)
      override val blockQuoteText: PaddingValues = PaddingValues(0.dp)
      override val codeBlock: PaddingValues = PaddingValues(0.dp)
      override val list: Dp = 0.dp
      override val listIndent: Dp = 0.dp
      override val listItemBottom: Dp = 0.dp
      override val listItemTop: Dp = 0.dp
    },
  )
}

fun Int.toHapticFeedbackType(): HapticFeedbackType {
  return when (this) {
    1, 2 -> HapticFeedbackType.TextHandleMove
    4, 5 -> HapticFeedbackType.Confirm
    else -> HapticFeedbackType.LongPress
  }
}
