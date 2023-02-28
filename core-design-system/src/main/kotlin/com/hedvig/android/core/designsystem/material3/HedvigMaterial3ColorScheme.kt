package com.hedvig.android.core.designsystem.material3

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Suppress("RemoveExplicitTypeArguments")
internal val LocalHedvigMaterial3ColorScheme = staticCompositionLocalOf<HedvigMaterial3ColorScheme> {
  lightHedvigColorScheme(LightColorScheme)
}

class HedvigMaterial3ColorScheme(
  val containedButtonContainer: Color,
  val onContainedButtonContainer: Color,
)

internal fun darkHedvigColorScheme(
  colorScheme: ColorScheme,
) = HedvigMaterial3ColorScheme(
  containedButtonContainer = colorScheme.tertiary,
  onContainedButtonContainer = colorScheme.onTertiary,
)

internal fun lightHedvigColorScheme(
  colorScheme: ColorScheme,
) = HedvigMaterial3ColorScheme(
  containedButtonContainer = colorScheme.primary,
  onContainedButtonContainer = colorScheme.onPrimary,
)
