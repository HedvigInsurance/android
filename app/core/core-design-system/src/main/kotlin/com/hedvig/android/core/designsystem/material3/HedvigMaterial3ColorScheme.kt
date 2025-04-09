@file:Suppress("UnusedReceiverParameter")

package com.hedvig.android.core.designsystem.material3

import android.annotation.SuppressLint
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.hedvig.android.core.designsystem.newtheme.hedvigTonalPalette

@SuppressLint("ComposeCompositionLocalUsage")
internal val LocalHedvigMaterial3ColorScheme = staticCompositionLocalOf<HedvigMaterial3ColorScheme> {
  lightHedvigColorScheme(hedvigTonalPalette, lightColorScheme())
}

class HedvigMaterial3ColorScheme(
  val primary: Color,
  val onPrimary: Color,
  val primaryContainer: Color,
  val onPrimaryContainer: Color,
  val inversePrimary: Color,
  val secondary: Color,
  val onSecondary: Color,
  val secondaryContainer: Color,
  val onSecondaryContainer: Color,
  val tertiary: Color,
  val onTertiary: Color,
  val tertiaryContainer: Color,
  val onTertiaryContainer: Color,
  val background: Color,
  val onBackground: Color,
  val surface: Color,
  val onSurface: Color,
  val surfaceVariant: Color,
  val onSurfaceVariant: Color,
  val surfaceTint: Color,
  val inverseSurface: Color,
  val inverseOnSurface: Color,
  val error: Color,
  val onError: Color,
  val errorContainer: Color,
  val onErrorContainer: Color,
  val outline: Color,
  val outlineVariant: Color,
  val scrim: Color,
  val surfaceBright: Color,
  val surfaceDim: Color,
  val surfaceContainer: Color,
  val surfaceContainerHigh: Color,
  val surfaceContainerHighest: Color,
  val surfaceContainerLow: Color,
  val surfaceContainerLowest: Color,
  val containedButtonContainer: Color,
  val onContainedButtonContainer: Color,
  val secondaryContainedButtonContainer: Color,
  val onSecondaryContainedButtonContainer: Color,
  val alwaysBlackContainer: Color,
  val onAlwaysBlackContainer: Color,
  val warningElement: Color,
  val onWarningElement: Color,
  val warningContainer: Color,
  val onWarningContainer: Color,
  val warningHighlight: Color,
  // Type refers to "typing" aka when the member is typing something in a text-field. Naming has room for improvement.
  val typeElement: Color,
  val onTypeElement: Color,
  val typeContainer: Color,
  val onTypeContainer: Color,
  val lightTypeContainer: Color,
  val onLightTypeContainer: Color,
  val typeHighlight: Color,
  // Information related content. Like information cards and so on.
  val infoElement: Color,
  val onInfoElement: Color,
  val infoContainer: Color,
  val onInfoContainer: Color,
  val infoHighlight: Color,
  // Help-center
  val yellowContainer: Color,
  val onYellowContainer: Color,
  val pinkContainer: Color,
  val onPinkContainer: Color,
  val purpleContainer: Color,
  val onPurpleContainer: Color,
  // The new Self-service flows
  val borderSecondary: Color,
)
