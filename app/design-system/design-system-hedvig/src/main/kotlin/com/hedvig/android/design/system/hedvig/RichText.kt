package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.halilibo.richtext.ui.BasicRichText
import com.halilibo.richtext.ui.LinkClickHandler
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.RichTextThemeProvider

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
  RichTextHedvigTheme {
    BasicRichText(
      modifier = modifier,
      style = style,
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
