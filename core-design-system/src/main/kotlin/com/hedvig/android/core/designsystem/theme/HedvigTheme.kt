package com.hedvig.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Colors
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.hedvig.android.core.designsystem.material2.HedvigMaterial2Theme
import com.hedvig.android.core.designsystem.material3.HedvigMaterial3Theme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HedvigTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  colorOverrides: (Colors) -> Colors = { it },
  m3ColorOverrides: (ColorScheme) -> ColorScheme = { it },
  content: @Composable () -> Unit,
) {
  Box(
    modifier = Modifier.semantics {
      testTagsAsResourceId = true
    },
  ) {
    HedvigMaterial2Theme(
      darkTheme = darkTheme,
      colorOverrides = colorOverrides,
    ) {
      HedvigMaterial3Theme(
        darkTheme = darkTheme,
        colorOverrides = m3ColorOverrides,
        content = content,
      )
    }
  }
}

@Composable
fun HedvigTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  useNewColorScheme: Boolean,
  content: @Composable () -> Unit,
) {
  HedvigTheme(
    darkTheme = darkTheme,
    m3ColorOverrides = if (useNewColorScheme && !darkTheme) {
      { oldColorScheme ->
        oldColorScheme.copy(
          background = oldColorScheme.surface,
          onBackground = oldColorScheme.onSurface,
          surface = oldColorScheme.background,
          onSurface = oldColorScheme.onBackground,
        )
      }
    } else {
      { it }
    },
  ) {
    content()
  }
}
