package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.BasicRichText
import com.halilibo.richtext.ui.LinkClickHandler
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.RichTextThemeProvider
import com.halilibo.richtext.ui.merge
import com.halilibo.richtext.ui.string.RichTextStringStyle

/**
 * RichText implementation that integrates with HedvigTheme.
 *
 */
@Composable
public fun RichText(
  modifier: Modifier = Modifier,
  style: RichTextStyle? = null,
  linkClickHandler: LinkClickHandler? = null,
  children: @Composable RichTextScope.() -> Unit,
) {
  val linkColor = HedvigTheme.colorScheme.link
  val linkStyle = RichTextStyle.Default.copy(
    stringStyle = RichTextStringStyle(
      linkStyle = SpanStyle(
        textDecoration = TextDecoration.Underline,
        color = linkColor,
      ),
    ),
  )
  RichTextHedvigTheme {
    BasicRichText(
      modifier = modifier,
      style = linkStyle.merge(style),
      linkClickHandler = linkClickHandler,
      children = children,
    )
  }
}

/**
 * Wraps the given [child] with Hedvig Theme integration for [BasicRichText].
 *
 * This function also keeps track of the parent context by using CompositionLocals
 * to not apply Material Theming if it already exists in the current composition.
 */
@Composable
private fun RichTextHedvigTheme(child: @Composable () -> Unit) {
  val isApplied = LocalThemingApplied.current

  if (!isApplied) {
    RichTextThemeProvider(
      textStyleProvider = { LocalTextStyle.current },
      contentColorProvider = { LocalContentColor.current },
      textStyleBackProvider = { textStyle, content ->
        ProvideTextStyle(textStyle, content)
      },
      contentColorBackProvider = { color, content ->
        CompositionLocalProvider(LocalContentColor provides color) {
          content()
        }
      },
    ) {
      CompositionLocalProvider(LocalThemingApplied provides true) {
        child()
      }
    }
  } else {
    child()
  }
}

private val LocalThemingApplied = compositionLocalOf { false }
