@file:Suppress("UnusedReceiverParameter")

package com.hedvig.android.core.designsystem.material3

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.hedvig.android.core.designsystem.component.tokens.HedvigColorSchemeKeyTokens
import com.hedvig.android.core.designsystem.newtheme.amber_100
import com.hedvig.android.core.designsystem.newtheme.amber_300
import com.hedvig.android.core.designsystem.newtheme.amber_600
import com.hedvig.android.core.designsystem.newtheme.amber_800

@Suppress("RemoveExplicitTypeArguments")
internal val LocalHedvigMaterial3ColorScheme = staticCompositionLocalOf<HedvigMaterial3ColorScheme> {
  lightHedvigColorScheme(LightColorScheme)
}

class HedvigMaterial3ColorScheme(
  val containedButtonContainer: Color,
  val onContainedButtonContainer: Color,
  val warningElement: Color,
  val warningSignal: Color,
  val onWarningSignal: Color,
  val warningSignalVariant: Color,
  val onWarningSignalVariant: Color,
)

internal fun darkHedvigColorScheme(
  colorScheme: ColorScheme,
) = HedvigMaterial3ColorScheme(
  containedButtonContainer = colorScheme.tertiary,
  onContainedButtonContainer = colorScheme.onTertiary,
  warningElement = amber_600,
  warningSignal = amber_300,
  onWarningSignal = amber_800,
  warningSignalVariant = amber_100,
  onWarningSignalVariant = amber_800,
)

internal fun lightHedvigColorScheme(
  colorScheme: ColorScheme,
) = HedvigMaterial3ColorScheme(
  containedButtonContainer = colorScheme.primary,
  onContainedButtonContainer = colorScheme.onPrimary,
  // Dark mode doesn't have distinct colors for warning* for now(?).
  warningElement = amber_600,
  warningSignal = amber_300,
  onWarningSignal = amber_800,
  warningSignalVariant = amber_100,
  onWarningSignalVariant = amber_800,
)

val ColorScheme.containedButtonContainer: Color
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.containedButtonContainer

val ColorScheme.onContainedButtonContainer: Color
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onContainedButtonContainer

val ColorScheme.warningElement: Color
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.warningElement

val ColorScheme.warningSignal: Color
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.warningSignal

val ColorScheme.onWarningSignal: Color
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onWarningSignal

val ColorScheme.warningSignalVariant: Color
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.warningSignalVariant

val ColorScheme.onWarningSignalVariant: Color
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onWarningSignalVariant

/**
 * Helper function for component color tokens. Here is an example on how to use component color
 * tokens:
 * `MaterialTheme.colorScheme.fromToken(HedvigColorSchemeKeyTokens.Background)`
 */
internal fun ColorScheme.fromToken(value: HedvigColorSchemeKeyTokens): Color {
  return when (value) {
    HedvigColorSchemeKeyTokens.Background -> background
    HedvigColorSchemeKeyTokens.Error -> error
    HedvigColorSchemeKeyTokens.ErrorContainer -> errorContainer
    HedvigColorSchemeKeyTokens.InverseOnSurface -> inverseOnSurface
    HedvigColorSchemeKeyTokens.InversePrimary -> inversePrimary
    HedvigColorSchemeKeyTokens.InverseSurface -> inverseSurface
    HedvigColorSchemeKeyTokens.OnBackground -> onBackground
    HedvigColorSchemeKeyTokens.OnError -> onError
    HedvigColorSchemeKeyTokens.OnErrorContainer -> onErrorContainer
    HedvigColorSchemeKeyTokens.OnPrimary -> onPrimary
    HedvigColorSchemeKeyTokens.OnPrimaryContainer -> onPrimaryContainer
    HedvigColorSchemeKeyTokens.OnSecondary -> onSecondary
    HedvigColorSchemeKeyTokens.OnSecondaryContainer -> onSecondaryContainer
    HedvigColorSchemeKeyTokens.OnSurface -> onSurface
    HedvigColorSchemeKeyTokens.OnSurfaceVariant -> onSurfaceVariant
    HedvigColorSchemeKeyTokens.SurfaceTint -> surfaceTint
    HedvigColorSchemeKeyTokens.OnTertiary -> onTertiary
    HedvigColorSchemeKeyTokens.OnTertiaryContainer -> onTertiaryContainer
    HedvigColorSchemeKeyTokens.Outline -> outline
    HedvigColorSchemeKeyTokens.OutlineVariant -> outlineVariant
    HedvigColorSchemeKeyTokens.Primary -> primary
    HedvigColorSchemeKeyTokens.PrimaryContainer -> primaryContainer
    HedvigColorSchemeKeyTokens.Scrim -> scrim
    HedvigColorSchemeKeyTokens.Secondary -> secondary
    HedvigColorSchemeKeyTokens.SecondaryContainer -> secondaryContainer
    HedvigColorSchemeKeyTokens.Surface -> surface
    HedvigColorSchemeKeyTokens.SurfaceVariant -> surfaceVariant
    HedvigColorSchemeKeyTokens.Tertiary -> tertiary
    HedvigColorSchemeKeyTokens.TertiaryContainer -> tertiaryContainer
  }
}

/**
 * A low level of alpha used to represent disabled components, such as text in a disabled Button.
 */
internal const val DisabledAlpha = 0.38f

/** Converts a color token key to the local color scheme provided by the theme */
@ReadOnlyComposable
@Composable
internal fun HedvigColorSchemeKeyTokens.toColor(): Color {
  return MaterialTheme.colorScheme.fromToken(this)
}
