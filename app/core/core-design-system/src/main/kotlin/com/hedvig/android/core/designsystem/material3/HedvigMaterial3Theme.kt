package com.hedvig.android.core.designsystem.material3

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.hedvig.android.core.designsystem.newtheme.amber_100
import com.hedvig.android.core.designsystem.newtheme.amber_400
import com.hedvig.android.core.designsystem.newtheme.amber_600
import com.hedvig.android.core.designsystem.newtheme.amber_800
import com.hedvig.android.core.designsystem.newtheme.blue_100
import com.hedvig.android.core.designsystem.newtheme.blue_400
import com.hedvig.android.core.designsystem.newtheme.blue_600
import com.hedvig.android.core.designsystem.newtheme.blue_800
import com.hedvig.android.core.designsystem.newtheme.blue_900
import com.hedvig.android.core.designsystem.newtheme.green_100
import com.hedvig.android.core.designsystem.newtheme.green_400
import com.hedvig.android.core.designsystem.newtheme.green_600
import com.hedvig.android.core.designsystem.newtheme.green_800
import com.hedvig.android.core.designsystem.newtheme.greyscale_0
import com.hedvig.android.core.designsystem.newtheme.greyscale_1000
import com.hedvig.android.core.designsystem.newtheme.hedvigTonalPalette
import com.hedvig.android.core.designsystem.theme.lavender_300
import com.hedvig.android.core.designsystem.theme.lavender_400

@Composable
internal fun HedvigMaterial3Theme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val (colorScheme, hedvigColorTheme) = when {
    darkTheme -> DarkColorScheme to darkHedvigColorScheme(DarkColorScheme)
    else -> LightColorScheme to lightHedvigColorScheme(LightColorScheme)
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

internal val LightColorScheme = lightColorScheme(
  primary = hedvigTonalPalette.greyscale1000,
  onPrimary = hedvigTonalPalette.greyscale0,
  inversePrimary = hedvigTonalPalette.greyscale800,
  primaryContainer = hedvigTonalPalette.greyscale50,
  onPrimaryContainer = hedvigTonalPalette.greyscale1000,

  secondary = hedvigTonalPalette.greyscale800,
  secondaryContainer = hedvigTonalPalette.greyscale50,
  onSecondaryContainer = hedvigTonalPalette.greyscale1000,
  onSecondary = hedvigTonalPalette.greyscale0,

  tertiary = lavender_300,
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

  error = Color(0xFFDD2727),
  onError = hedvigTonalPalette.greyscale0,
  errorContainer = hedvigTonalPalette.greyscale50,
  onErrorContainer = hedvigTonalPalette.greyscale1000,

//  outline = no equivalent,
  outlineVariant = hedvigTonalPalette.greyscale300,
//  scrim = default is ok,
)

private val DarkColorScheme = darkColorScheme(
  primary = hedvigTonalPalette.greyscale0,
  onPrimary = hedvigTonalPalette.greyscale1000,
  inversePrimary = hedvigTonalPalette.greyscale50,
  primaryContainer = hedvigTonalPalette.greyscale900,
  onPrimaryContainer = hedvigTonalPalette.greyscale0,

  secondary = hedvigTonalPalette.greyscale50,
  onSecondary = hedvigTonalPalette.greyscale1000,
  secondaryContainer = hedvigTonalPalette.greyscale900,
  onSecondaryContainer = hedvigTonalPalette.greyscale0,

  tertiary = lavender_400,
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

  error = Color(0xFFE24646),
  onError = hedvigTonalPalette.greyscale0,
  errorContainer = hedvigTonalPalette.greyscale900,
  onErrorContainer = hedvigTonalPalette.greyscale0,

//  outline = no equivalent,
  outlineVariant = hedvigTonalPalette.greyscale700,
  scrim = hedvigTonalPalette.greyscale0,
)

internal fun darkHedvigColorScheme(
  colorScheme: ColorScheme,
) = HedvigMaterial3ColorScheme(
  containedButtonContainer = colorScheme.tertiary,
  onContainedButtonContainer = colorScheme.onTertiary,

  // In the comments are the light mode colors, showing the equivalent and how it was chosen
  warningElement = amber_400, // amber_600,
  onWarningElement = greyscale_0, // greyscale_1000,
  warningContainer = amber_800, // amber_100,
  onWarningContainer = amber_100, // amber_800,

  typeElement = green_400, // green_600,
  onTypeElement = greyscale_0, // greyscale_1000,
  typeContainer = green_800, // green_100,
  onTypeContainer = green_100, // green_800,

  infoElement = blue_400, // blue_600,
  onInfoElement = greyscale_1000, // greyscale_1000,
  infoContainer = blue_800, // blue_100,
  onInfoContainer = blue_100, // blue_900,
)

internal fun lightHedvigColorScheme(
  colorScheme: ColorScheme,
) = HedvigMaterial3ColorScheme(
  containedButtonContainer = colorScheme.primary,
  onContainedButtonContainer = colorScheme.onPrimary,

  warningElement = amber_600,
  onWarningElement = greyscale_1000,
  warningContainer = amber_100,
  onWarningContainer = amber_800,

  typeElement = green_600,
  onTypeElement = greyscale_1000,
  typeContainer = green_100,
  onTypeContainer = green_800,

  infoElement = blue_600,
  onInfoElement = greyscale_1000,
  infoContainer = blue_100,
  onInfoContainer = blue_900,
)
