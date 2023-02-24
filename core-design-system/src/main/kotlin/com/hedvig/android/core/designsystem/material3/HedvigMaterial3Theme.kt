package com.hedvig.android.core.designsystem.material3

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun HedvigMaterial3Theme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  colorOverrides: (ColorScheme) -> ColorScheme = { it },
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }
  MaterialTheme(
    colorScheme = colorOverrides.invoke(colorScheme),
    shapes = HedvigShapes,
    typography = HedvigTypography,
  ) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
      content()
    }
  }
}

@Suppress("PrivatePropertyName")
private val LightColorScheme = lightColorScheme(
  primary = light_primary,
  onPrimary = light_onPrimary,
  inversePrimary = light_primaryVariant,
  secondary = light_primaryVariant,
  onSecondary = light_onPrimary,
  tertiary = light_secondary,
  onTertiary = light_onSecondary,
  primaryContainer = light_surface,
  onPrimaryContainer = light_onSurface,
  secondaryContainer = light_surface,
  onSecondaryContainer = light_onSurface,
  tertiaryContainer = light_surface,
  onTertiaryContainer = light_onSurface,
  background = light_background,
  onBackground = light_onBackground,
  surface = light_surface,
  onSurface = light_onSurface,
  surfaceVariant = light_surfaceVariant,
  onSurfaceVariant = light_onSurfaceVariant,
  surfaceTint = light_surface, // no tint, as m2 also did not tint elevated surfaces
  inverseSurface = light_onSurface,
  inverseOnSurface = light_surface,
  error = light_error,
  onError = light_onError,
  errorContainer = light_surface,
  onErrorContainer = light_onSurface,
//  outline = no equivalent,
//  outlineVariant = no equivalent,
//  scrim = no equivalent,
)

@Suppress("PrivatePropertyName")
private val DarkColorScheme = darkColorScheme(
  primary = dark_primary,
  onPrimary = dark_onPrimary,
  inversePrimary = dark_primaryVariant,
  secondary = dark_primaryVariant,
  onSecondary = dark_onPrimary,
  tertiary = dark_secondary,
  onTertiary = dark_onSecondary,
  primaryContainer = dark_surface,
  onPrimaryContainer = dark_onSurface,
  secondaryContainer = dark_surface,
  onSecondaryContainer = dark_onSurface,
  tertiaryContainer = dark_surface,
  onTertiaryContainer = dark_onSurface,
  background = dark_background,
  onBackground = dark_onBackground,
  surface = dark_surface,
  onSurface = dark_onSurface,
  surfaceVariant = dark_surfaceVariant,
  onSurfaceVariant = dark_onSurfaceVariant,
  surfaceTint = dark_surface, // no tint, as m2 also did not tint elevated surfaces
  inverseSurface = dark_onSurface,
  inverseOnSurface = dark_surface,
  error = dark_error,
  onError = dark_onError,
  errorContainer = dark_surface,
  onErrorContainer = dark_onSurface,
//  outline = no equivalent,
//  outlineVariant = no equivalent,
//  scrim = no equivalent,
)
