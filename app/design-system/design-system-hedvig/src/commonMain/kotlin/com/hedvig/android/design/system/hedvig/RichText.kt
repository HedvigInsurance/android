package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownPadding
import com.mikepenz.markdown.model.MarkdownTypography

/**
 * Renders Markdown content with Hedvig theming.
 */
@Composable
fun HedvigMarkdownText(content: String, modifier: Modifier = Modifier) {
  val colors = HedvigTheme.colorScheme
  val typography = HedvigTheme.typography

  val markdownColors = object : MarkdownColors {
    override val text: Color = colors.textPrimary
    override val codeBackground: Color = colors.surfaceSecondary
    override val inlineCodeBackground: Color = colors.surfaceSecondary
    override val dividerColor: Color = colors.borderPrimary
    override val tableBackground: Color = colors.surfaceSecondary
  }

  val markdownTypography = object : MarkdownTypography {
    override val h1 = typography.headlineLarge.copy(color = colors.textPrimary)
    override val h2 = typography.headlineMedium.copy(color = colors.textPrimary)
    override val h3 = typography.headlineSmall.copy(color = colors.textPrimary)
    override val h4 = typography.displaySmall.copy(color = colors.textPrimary)
    override val h5 = typography.bodyLarge.copy(color = colors.textPrimary)
    override val h6 = typography.bodyMedium.copy(color = colors.textPrimary)
    override val text = typography.bodySmall.copy(color = colors.textPrimary)
    override val paragraph = typography.bodySmall.copy(color = colors.textPrimary)
    override val code = typography.label
    override val inlineCode = typography.label
    override val bullet = typography.bodySmall.copy(color = colors.textPrimary)
    override val list = typography.bodySmall.copy(color = colors.textPrimary)
    override val ordered = typography.bodySmall.copy(color = colors.textPrimary)
    override val quote = typography.bodySmall.copy(color = colors.textPrimary)
    override val table = typography.bodySmall.copy(color = colors.textPrimary)
    override val textLink = TextLinkStyles(
      style = typography.bodySmall.copy(color = colors.link).toSpanStyle(),
      focusedStyle = typography.bodySmall.copy(color = colors.link).toSpanStyle(),
      hoveredStyle = typography.bodySmall.copy(color = colors.link).toSpanStyle(),
      pressedStyle = typography.bodySmall.copy(color = colors.link).toSpanStyle(),
    )
  }

  Markdown(
    content = content,
    modifier = modifier,
    colors = markdownColors,
    typography = markdownTypography,
    padding = object : MarkdownPadding {
      override val block = 0.dp
      override val blockQuote = PaddingValues(0.dp)
      override val blockQuoteBar = PaddingValues.Absolute(0.dp)
      override val blockQuoteText = PaddingValues(0.dp)
      override val codeBlock = PaddingValues(0.dp)
      override val list = 0.dp
      override val listIndent = 0.dp
      override val listItemBottom = 0.dp
      override val listItemTop = 0.dp
    },
  )
}

/**
 * [tag] is also used to name the a11y action, so it must be an appropriate user-facing copy
 */
@Composable
inline fun <R : Any> AnnotatedString.Builder.withHedvigLink(
  tag: String,
  noinline onClick: () -> Unit,
  block: AnnotatedString.Builder.() -> R,
): R {
  return withLink(
    LinkAnnotation.Clickable(
      tag = tag,
      linkInteractionListener = { onClick() },
      styles = TextLinkStyles(
        SpanStyle(
          textDecoration = TextDecoration.Underline,
          color = HedvigTheme.colorScheme.link,
        ),
      ),
    ),
  ) {
    block()
  }
}

@Composable
inline fun <R : Any> AnnotatedString.Builder.withHedvigLink(url: String, block: AnnotatedString.Builder.() -> R): R {
  return withLink(
    LinkAnnotation.Url(
      url = url,
      styles = TextLinkStyles(
        SpanStyle(
          textDecoration = TextDecoration.Underline,
          color = HedvigTheme.colorScheme.link,
        ),
      ),
    ),
  ) {
    block()
  }
}
