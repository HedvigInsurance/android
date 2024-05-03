package com.hedvig.android.design.system.hedvig.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.ProvideTextStyle

/**
 * Set content color, typography and emphasis for [content] composable
 */
@Composable
internal fun Decoration(contentColor: Color, typography: TextStyle? = null, content: @Composable () -> Unit) {
  val contentWithColor: @Composable () -> Unit = @Composable {
    CompositionLocalProvider(
      LocalContentColor provides contentColor,
      content = content,
    )
  }
  if (typography != null) ProvideTextStyle(typography, contentWithColor) else contentWithColor()
}
