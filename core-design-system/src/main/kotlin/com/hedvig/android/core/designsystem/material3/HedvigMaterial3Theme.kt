package com.hedvig.android.core.designsystem.material3

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.hedvig.android.core.designsystem.theme.dark_background
import com.hedvig.android.core.designsystem.theme.dark_error
import com.hedvig.android.core.designsystem.theme.dark_onBackground
import com.hedvig.android.core.designsystem.theme.dark_onError
import com.hedvig.android.core.designsystem.theme.dark_onPrimary
import com.hedvig.android.core.designsystem.theme.dark_onSecondary
import com.hedvig.android.core.designsystem.theme.dark_onSurface
import com.hedvig.android.core.designsystem.theme.dark_onSurfaceVariant
import com.hedvig.android.core.designsystem.theme.dark_outlineVariant
import com.hedvig.android.core.designsystem.theme.dark_primary
import com.hedvig.android.core.designsystem.theme.dark_primaryVariant
import com.hedvig.android.core.designsystem.theme.dark_secondary
import com.hedvig.android.core.designsystem.theme.dark_surface
import com.hedvig.android.core.designsystem.theme.dark_surfaceVariant
import com.hedvig.android.core.designsystem.theme.light_background
import com.hedvig.android.core.designsystem.theme.light_error
import com.hedvig.android.core.designsystem.theme.light_onBackground
import com.hedvig.android.core.designsystem.theme.light_onError
import com.hedvig.android.core.designsystem.theme.light_onPrimary
import com.hedvig.android.core.designsystem.theme.light_onSecondary
import com.hedvig.android.core.designsystem.theme.light_onSurface
import com.hedvig.android.core.designsystem.theme.light_onSurfaceVariant
import com.hedvig.android.core.designsystem.theme.light_outlineVariant
import com.hedvig.android.core.designsystem.theme.light_primary
import com.hedvig.android.core.designsystem.theme.light_primaryVariant
import com.hedvig.android.core.designsystem.theme.light_secondary
import com.hedvig.android.core.designsystem.theme.light_surface
import com.hedvig.android.core.designsystem.theme.light_surfaceVariant

@Composable
internal fun HedvigMaterial3Theme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  colorOverrides: (ColorScheme) -> ColorScheme = { it },
  content: @Composable () -> Unit,
) {
  val (colorScheme, hedvigColorTheme) = when {
    darkTheme -> DarkColorScheme to darkHedvigColorScheme(DarkColorScheme)
    else -> LightColorScheme to lightHedvigColorScheme(LightColorScheme)
  }
  MaterialTheme(
    colorScheme = colorOverrides.invoke(colorScheme),
    shapes = HedvigShapes,
    typography = HedvigTypography,
  ) {
    CompositionLocalProvider(
      LocalContentColor provides MaterialTheme.colorScheme.onBackground,
      LocalHedvigMaterial3ColorScheme provides hedvigColorTheme,
    ) {
      content()
    }
  }
}

internal val LightColorScheme = lightColorScheme(
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
  outlineVariant = light_outlineVariant,
//  scrim = no equivalent,
)

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
  outlineVariant = dark_outlineVariant,
//  scrim = no equivalent,
)
