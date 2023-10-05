package com.hedvig.android.core.designsystem.material3

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.hedvig.android.core.designsystem.newtheme.HedvigTonalPalette
import com.hedvig.android.core.designsystem.newtheme.hedvigTonalPalette

@Composable
internal fun HedvigMaterial3Theme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val hedvigTonalPalette = hedvigTonalPalette
  val (colorScheme, hedvigColorTheme) = when {
    darkTheme -> {
      val darkColorScheme = darkColorScheme(hedvigTonalPalette)
      darkColorScheme to darkHedvigColorScheme(hedvigTonalPalette, darkColorScheme)
    }
    else -> {
      val lightColorScheme = lightColorScheme(hedvigTonalPalette)
      lightColorScheme to lightHedvigColorScheme(hedvigTonalPalette, lightColorScheme)
    }
  }
  MaterialTheme(
    colorScheme = colorScheme,
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

internal fun lightColorScheme(hedvigTonalPalette: HedvigTonalPalette) = lightColorScheme(
  primary = hedvigTonalPalette.greyscale1000,
  onPrimary = hedvigTonalPalette.greyscale0,
  inversePrimary = hedvigTonalPalette.greyscale800,
  primaryContainer = hedvigTonalPalette.greyscale50,
  onPrimaryContainer = hedvigTonalPalette.greyscale1000,

  secondary = hedvigTonalPalette.greyscale800,
  secondaryContainer = hedvigTonalPalette.greyscale50,
  onSecondaryContainer = hedvigTonalPalette.greyscale1000,
  onSecondary = hedvigTonalPalette.greyscale0,

  tertiary = hedvigTonalPalette.lavender300,
  onTertiary = hedvigTonalPalette.greyscale1000,
  tertiaryContainer = hedvigTonalPalette.greyscale50,
  onTertiaryContainer = hedvigTonalPalette.greyscale1000,

  background = hedvigTonalPalette.greyscale0,
  onBackground = hedvigTonalPalette.greyscale1000,

  surface = hedvigTonalPalette.greyscale50,
  onSurface = hedvigTonalPalette.greyscale1000,
  surfaceVariant = hedvigTonalPalette.greyscale100,
  onSurfaceVariant = hedvigTonalPalette.greyscale700,
  surfaceTint = hedvigTonalPalette.greyscale50, // no tint, as m2 also did not tint elevated surfaces
  inverseSurface = hedvigTonalPalette.greyscale1000,
  inverseOnSurface = hedvigTonalPalette.greyscale50,

  error = hedvigTonalPalette.red600,
  onError = hedvigTonalPalette.greyscale0,
  errorContainer = hedvigTonalPalette.greyscale50,
  onErrorContainer = hedvigTonalPalette.greyscale1000,

//  outline = no equivalent,
  outlineVariant = hedvigTonalPalette.greyscale300,
//  scrim = default is ok,
)

private fun darkColorScheme(hedvigTonalPalette: HedvigTonalPalette) = darkColorScheme(
  primary = hedvigTonalPalette.greyscale0,
  onPrimary = hedvigTonalPalette.greyscale1000,
  inversePrimary = hedvigTonalPalette.greyscale50,
  primaryContainer = hedvigTonalPalette.greyscale900,
  onPrimaryContainer = hedvigTonalPalette.greyscale0,

  secondary = hedvigTonalPalette.greyscale50,
  onSecondary = hedvigTonalPalette.greyscale1000,
  secondaryContainer = hedvigTonalPalette.greyscale900,
  onSecondaryContainer = hedvigTonalPalette.greyscale0,

  tertiary = hedvigTonalPalette.lavender400,
  onTertiary = hedvigTonalPalette.greyscale1000,
  tertiaryContainer = hedvigTonalPalette.greyscale900,
  onTertiaryContainer = hedvigTonalPalette.greyscale0,

  background = hedvigTonalPalette.greyscale1000,
  onBackground = hedvigTonalPalette.greyscale0,

  surface = hedvigTonalPalette.greyscale900,
  onSurface = hedvigTonalPalette.greyscale0,
  surfaceVariant = hedvigTonalPalette.greyscale700,
  onSurfaceVariant = hedvigTonalPalette.greyscale400,
  surfaceTint = hedvigTonalPalette.greyscale900, // no tint, as m2 also did not tint elevated surfaces
  inverseSurface = hedvigTonalPalette.greyscale0,
  inverseOnSurface = hedvigTonalPalette.greyscale900,

  error = hedvigTonalPalette.red600,
  onError = hedvigTonalPalette.greyscale0,
  errorContainer = hedvigTonalPalette.greyscale900,
  onErrorContainer = hedvigTonalPalette.greyscale0,

//  outline = no equivalent,
  outlineVariant = hedvigTonalPalette.greyscale700,
  scrim = hedvigTonalPalette.greyscale0,
)

internal fun darkHedvigColorScheme(
  hedvigTonalPalette: HedvigTonalPalette,
  colorScheme: ColorScheme,
) = HedvigMaterial3ColorScheme(
  containedButtonContainer = colorScheme.tertiary,
  onContainedButtonContainer = colorScheme.onTertiary,

  // In the comments are the light mode colors, showing the equivalent and how it was chosen
  warningElement = hedvigTonalPalette.amber400, // amber600,
  onWarningElement = hedvigTonalPalette.greyscale0, // greyscale1000,
  warningContainer = hedvigTonalPalette.amber800, // amber100,
  onWarningContainer = hedvigTonalPalette.amber100, // amber800,

  typeElement = hedvigTonalPalette.green400, // green600,
  onTypeElement = hedvigTonalPalette.greyscale0, // greyscale1000,
  typeContainer = hedvigTonalPalette.green800, // green100,
  onTypeContainer = hedvigTonalPalette.green100, // green800,

  infoElement = hedvigTonalPalette.blue400, // blue600,
  onInfoElement = hedvigTonalPalette.greyscale1000, // greyscale1000,
  infoContainer = hedvigTonalPalette.blue800, // blue100,
  onInfoContainer = hedvigTonalPalette.blue100, // blue900,
)

internal fun lightHedvigColorScheme(
  hedvigTonalPalette: HedvigTonalPalette,
  colorScheme: ColorScheme,
) = HedvigMaterial3ColorScheme(
  containedButtonContainer = colorScheme.primary,
  onContainedButtonContainer = colorScheme.onPrimary,

  warningElement = hedvigTonalPalette.amber600,
  onWarningElement = hedvigTonalPalette.greyscale1000,
  warningContainer = hedvigTonalPalette.amber100,
  onWarningContainer = hedvigTonalPalette.amber800,

  typeElement = hedvigTonalPalette.green600,
  onTypeElement = hedvigTonalPalette.greyscale1000,
  typeContainer = hedvigTonalPalette.green100,
  onTypeContainer = hedvigTonalPalette.green800,

  infoElement = hedvigTonalPalette.blue600,
  onInfoElement = hedvigTonalPalette.greyscale1000,
  infoContainer = hedvigTonalPalette.blue100,
  onInfoContainer = hedvigTonalPalette.blue900,
)
