package com.hedvig.android.core.designsystem.material3

import androidx.compose.foundation.isSystemInDarkTheme
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
      darkColorScheme(hedvigTonalPalette) to darkHedvigColorScheme(hedvigTonalPalette)
    }
    else -> {
      lightColorScheme(hedvigTonalPalette) to lightHedvigColorScheme(hedvigTonalPalette)
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
  onPrimary = hedvigTonalPalette.greyscale25,
  inversePrimary = hedvigTonalPalette.greyscale800,
  primaryContainer = hedvigTonalPalette.greyscale50,
  onPrimaryContainer = hedvigTonalPalette.greyscale1000,

  secondary = hedvigTonalPalette.greyscale800,
  secondaryContainer = hedvigTonalPalette.greyscale50,
  onSecondaryContainer = hedvigTonalPalette.greyscale1000,
  onSecondary = hedvigTonalPalette.greyscale25,

  tertiary = hedvigTonalPalette.lavender300,
  onTertiary = hedvigTonalPalette.greyscale1000,
  tertiaryContainer = hedvigTonalPalette.greyscale50,
  onTertiaryContainer = hedvigTonalPalette.greyscale1000,

  background = hedvigTonalPalette.greyscale25,
  onBackground = hedvigTonalPalette.greyscale1000,

  surface = hedvigTonalPalette.greyscale100,
  onSurface = hedvigTonalPalette.greyscale1000,
  surfaceVariant = hedvigTonalPalette.greyscale200,
  onSurfaceVariant = hedvigTonalPalette.greyscale700,
  surfaceTint = hedvigTonalPalette.greyscale50, // no tint, as m2 also did not tint elevated surfaces
  inverseSurface = hedvigTonalPalette.greyscale1000,
  inverseOnSurface = hedvigTonalPalette.greyscale50,

  error = hedvigTonalPalette.red600,
  onError = hedvigTonalPalette.greyscale25,
  errorContainer = hedvigTonalPalette.greyscale50,
  onErrorContainer = hedvigTonalPalette.greyscale1000,

//  outline = no equivalent,
  outlineVariant = hedvigTonalPalette.greyscale300,
//  scrim = default is ok,
)

private fun darkColorScheme(hedvigTonalPalette: HedvigTonalPalette) = darkColorScheme(
  primary = hedvigTonalPalette.greyscale25,
  onPrimary = hedvigTonalPalette.greyscale1000,
  inversePrimary = hedvigTonalPalette.greyscale50,
  primaryContainer = hedvigTonalPalette.greyscale900,
  onPrimaryContainer = hedvigTonalPalette.greyscale25,

  secondary = hedvigTonalPalette.greyscale50,
  onSecondary = hedvigTonalPalette.greyscale1000,
  secondaryContainer = hedvigTonalPalette.greyscale900,
  onSecondaryContainer = hedvigTonalPalette.greyscale25,

  tertiary = hedvigTonalPalette.lavender400,
  onTertiary = hedvigTonalPalette.greyscale1000,
  tertiaryContainer = hedvigTonalPalette.greyscale900,
  onTertiaryContainer = hedvigTonalPalette.greyscale25,

  background = hedvigTonalPalette.greyscale1000,
  onBackground = hedvigTonalPalette.greyscale25,

  surface = hedvigTonalPalette.greyscale900,
  onSurface = hedvigTonalPalette.greyscale25,
  surfaceVariant = hedvigTonalPalette.greyscale700,
  onSurfaceVariant = hedvigTonalPalette.greyscale400,
  surfaceTint = hedvigTonalPalette.greyscale900, // no tint, as m2 also did not tint elevated surfaces
  inverseSurface = hedvigTonalPalette.greyscale100,
  inverseOnSurface = hedvigTonalPalette.greyscale1000,

  error = hedvigTonalPalette.red600,
  onError = hedvigTonalPalette.greyscale25,
  errorContainer = hedvigTonalPalette.greyscale900,
  onErrorContainer = hedvigTonalPalette.greyscale25,

//  outline = no equivalent,
  outlineVariant = hedvigTonalPalette.greyscale700,
  scrim = hedvigTonalPalette.greyscale25,
)

internal fun darkHedvigColorScheme(
  hedvigTonalPalette: HedvigTonalPalette,
) = HedvigMaterial3ColorScheme(
  containedButtonContainer = hedvigTonalPalette.greyscale25,
  onContainedButtonContainer = hedvigTonalPalette.greyscale1000,

  // In the comments are the light mode colors, showing the equivalent and how it was chosen
  warningElement = hedvigTonalPalette.amber700,
  onWarningElement = hedvigTonalPalette.greyscale25,
  warningContainer = hedvigTonalPalette.amber300,
  onWarningContainer = hedvigTonalPalette.amber900,
  warningHighlight = hedvigTonalPalette.amber500,

  typeElement = hedvigTonalPalette.green700,
  onTypeElement = hedvigTonalPalette.greyscale25,
  typeContainer = hedvigTonalPalette.green300,
  onTypeContainer = hedvigTonalPalette.green900,
  typeHighlight = hedvigTonalPalette.greyscale800,

  infoElement = hedvigTonalPalette.blue700,
  onInfoElement = hedvigTonalPalette.greyscale1000,
  infoContainer = hedvigTonalPalette.blue300,
  onInfoContainer = hedvigTonalPalette.blue900,
  infoHighlight = hedvigTonalPalette.blue500,
)

internal fun lightHedvigColorScheme(
  hedvigTonalPalette: HedvigTonalPalette,
) = HedvigMaterial3ColorScheme(
  containedButtonContainer = hedvigTonalPalette.greyscale25,
  onContainedButtonContainer = hedvigTonalPalette.greyscale1000,

  warningElement = hedvigTonalPalette.amber600,
  onWarningElement = hedvigTonalPalette.greyscale1000,
  warningContainer = hedvigTonalPalette.amber100,
  onWarningContainer = hedvigTonalPalette.amber800,
  warningHighlight = hedvigTonalPalette.amber300,

  typeElement = hedvigTonalPalette.green600,
  onTypeElement = hedvigTonalPalette.greyscale1000,
  typeContainer = hedvigTonalPalette.green100,
  onTypeContainer = hedvigTonalPalette.green800,
  typeHighlight = hedvigTonalPalette.green300,

  infoElement = hedvigTonalPalette.blue600,
  onInfoElement = hedvigTonalPalette.greyscale1000,
  infoContainer = hedvigTonalPalette.blue100,
  onInfoContainer = hedvigTonalPalette.blue900,
  infoHighlight = hedvigTonalPalette.blue300,
)
