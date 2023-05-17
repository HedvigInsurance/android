package com.hedvig.android.core.designsystem.material2

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.hedvig.android.core.designsystem.theme.dark_background
import com.hedvig.android.core.designsystem.theme.dark_error
import com.hedvig.android.core.designsystem.theme.dark_onBackground
import com.hedvig.android.core.designsystem.theme.dark_onError
import com.hedvig.android.core.designsystem.theme.dark_onPrimary
import com.hedvig.android.core.designsystem.theme.dark_onSecondary
import com.hedvig.android.core.designsystem.theme.dark_onSurface
import com.hedvig.android.core.designsystem.theme.dark_primary
import com.hedvig.android.core.designsystem.theme.dark_primaryVariant
import com.hedvig.android.core.designsystem.theme.dark_secondary
import com.hedvig.android.core.designsystem.theme.dark_surface
import com.hedvig.android.core.designsystem.theme.light_background
import com.hedvig.android.core.designsystem.theme.light_error
import com.hedvig.android.core.designsystem.theme.light_onBackground
import com.hedvig.android.core.designsystem.theme.light_onError
import com.hedvig.android.core.designsystem.theme.light_onPrimary
import com.hedvig.android.core.designsystem.theme.light_onSecondary
import com.hedvig.android.core.designsystem.theme.light_onSurface
import com.hedvig.android.core.designsystem.theme.light_primary
import com.hedvig.android.core.designsystem.theme.light_primaryVariant
import com.hedvig.android.core.designsystem.theme.light_secondary
import com.hedvig.android.core.designsystem.theme.light_surface

@Composable
internal fun HedvigMaterial2Theme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  colorOverrides: (Colors) -> Colors = { it },
  content: @Composable () -> Unit,
) {
  val colors = when {
    darkTheme -> DarkColors
    else -> LightColors
  }
  MaterialTheme(
    colors = colorOverrides.invoke(colors),
    shapes = HedvigShapes,
    typography = HedvigTypography,
  ) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onBackground) {
      content()
    }
  }
}

private val LightColors = lightColors(
  primary = light_primary,
  primaryVariant = light_primaryVariant,
  secondary = light_secondary,
// we are not using secondaryVariant https://github.com/HedvigInsurance/android/blob/develop/app/src/main/res/values/theme.xml#L12
//  secondaryVariant = ...,
  background = light_background,
  surface = light_surface,
  error = light_error,
  onPrimary = light_onPrimary,
  onSecondary = light_onSecondary,
  onBackground = light_onBackground,
  onSurface = light_onSurface,
  onError = light_onError,
)

private val DarkColors = darkColors(
  primary = dark_primary,
  primaryVariant = dark_primaryVariant,
  secondary = dark_secondary,
// we are not using secondaryVariant https://github.com/HedvigInsurance/android/blob/develop/app/src/main/res/values/theme.xml#L12
//  secondaryVariant = ...,
  background = dark_background,
  surface = dark_surface,
  error = dark_error,
  onPrimary = dark_onPrimary,
  onSecondary = dark_onSecondary,
  onBackground = dark_onBackground,
  onSurface = dark_onSurface,
  onError = dark_onError,
)
