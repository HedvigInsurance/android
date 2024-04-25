package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

@Composable
fun HedvigTheme(
  colorScheme: ColorScheme = HedvigTheme.colorScheme,
  shapes: Shapes = HedvigTheme.shapes,
  typography: Typography = HedvigTheme.typography,
  content: @Composable () -> Unit,
) {
  val selectionColors = rememberTextSelectionColors(colorScheme)
  CompositionLocalProvider(
    LocalColorScheme provides colorScheme,
    LocalIndication provides ripple(),
    LocalShapes provides shapes,
    LocalTextSelectionColors provides selectionColors,
    LocalTypography provides typography,
  ) {
    ProvideTextStyle(value = typography.bodySmall, content = content)
  }
}

object HedvigTheme {
  /**
   * Retrieves the current [ColorScheme] at the call site's position in the hierarchy.
   */
  val colorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalColorScheme.current

  /**
   * Retrieves the current [Typography] at the call site's position in the hierarchy.
   */
  val typography: Typography
    @Composable
    @ReadOnlyComposable
    get() = LocalTypography.current

  /**
   * Retrieves the current [Shapes] at the call site's position in the hierarchy.
   */
  val shapes: Shapes
    @Composable
    @ReadOnlyComposable
    get() = LocalShapes.current
}

@Composable
private fun rememberTextSelectionColors(colorScheme: ColorScheme): TextSelectionColors {
  val primaryColor = colorScheme.textPrimary
  return remember(primaryColor) {
    TextSelectionColors(
      handleColor = primaryColor,
      backgroundColor = primaryColor.copy(alpha = TextSelectionBackgroundOpacity),
    )
  }
}

private const val TextSelectionBackgroundOpacity = 0.4f
