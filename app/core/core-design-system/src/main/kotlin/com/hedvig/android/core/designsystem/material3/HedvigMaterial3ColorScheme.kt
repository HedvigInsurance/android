@file:Suppress("UnusedReceiverParameter")

package com.hedvig.android.core.designsystem.material3

import android.annotation.SuppressLint
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.hedvig.android.core.designsystem.component.navigation.HedvigNavigationBarItemColors
import com.hedvig.android.core.designsystem.component.tokens.HedvigColorSchemeKeyTokens
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
) {
  internal var defaultNavigationBarItemColorsCached: HedvigNavigationBarItemColors? = null
}

val ColorScheme.containedButtonContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.containedButtonContainer

val ColorScheme.onContainedButtonContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onContainedButtonContainer

val ColorScheme.secondaryContainedButtonContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.secondaryContainedButtonContainer

val ColorScheme.onSecondaryContainedButtonContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onSecondaryContainedButtonContainer

val ColorScheme.alwaysBlackContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.alwaysBlackContainer

val ColorScheme.onAlwaysBlackContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onAlwaysBlackContainer

// region warning colors
val ColorScheme.warningElement: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.warningElement

val ColorScheme.onWarningElement: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onWarningElement

val ColorScheme.warningContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.warningContainer

val ColorScheme.onWarningContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onWarningContainer

val ColorScheme.warningHighlight: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.warningHighlight
// endregion

// region typing colors
val ColorScheme.typeElement: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.typeElement

val ColorScheme.onTypeElement: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onTypeElement

val ColorScheme.lightTypeContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.lightTypeContainer

val ColorScheme.onLightTypeContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onLightTypeContainer

val ColorScheme.typeContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.typeContainer

val ColorScheme.onTypeContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onTypeContainer

val ColorScheme.typeHighlight: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.typeHighlight
// endregion

// region info colors
val ColorScheme.infoElement: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.infoElement

val ColorScheme.onInfoElement: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onInfoElement

val ColorScheme.infoContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.infoContainer

val ColorScheme.borderSecondary: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.borderSecondary

val ColorScheme.onInfoContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onInfoContainer

val ColorScheme.infoHighlight: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.infoHighlight
// endregion

// region help-center colors
val ColorScheme.yellowContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.yellowContainer

val ColorScheme.onYellowContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onYellowContainer

val ColorScheme.purpleContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.purpleContainer

val ColorScheme.onPurpleContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onPurpleContainer

val ColorScheme.pinkContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.pinkContainer

val ColorScheme.onPinkContainer: Color
  @ReadOnlyComposable
  @Composable
  get() = LocalHedvigMaterial3ColorScheme.current.onPinkContainer
// endregion

/**
 * Helper function for component color tokens. Here is an example on how to use component color
 * tokens:
 * `LocalHedvigMaterial3ColorScheme.current.fromToken(HedvigColorSchemeKeyTokens.Background)`
 */
internal fun HedvigMaterial3ColorScheme.fromToken(value: HedvigColorSchemeKeyTokens): Color {
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

    HedvigColorSchemeKeyTokens.WarningContainer -> warningContainer
    HedvigColorSchemeKeyTokens.OnWarningContainer -> onWarningContainer
    HedvigColorSchemeKeyTokens.WarningElement -> warningElement
    HedvigColorSchemeKeyTokens.OnWarningElement -> onWarningElement
    HedvigColorSchemeKeyTokens.WarningHighlight -> warningHighlight

    HedvigColorSchemeKeyTokens.TypeElement -> typeElement
    HedvigColorSchemeKeyTokens.OnTypeElement -> onTypeElement
    HedvigColorSchemeKeyTokens.TypeContainer -> typeContainer
    HedvigColorSchemeKeyTokens.OnTypeContainer -> onTypeContainer
    HedvigColorSchemeKeyTokens.LightTypeContainer -> lightTypeContainer
    HedvigColorSchemeKeyTokens.OnLightTypeContainer -> onLightTypeContainer
    HedvigColorSchemeKeyTokens.TypeHighlight -> typeHighlight

    HedvigColorSchemeKeyTokens.BorderSecondary -> borderSecondary
  }
}

/** Converts a color token key to the local color scheme provided by the theme */
@ReadOnlyComposable
@Composable
internal fun HedvigColorSchemeKeyTokens.toColor(): Color {
  return LocalHedvigMaterial3ColorScheme.current.fromToken(this)
}

/**
 * A low level of alpha used to represent disabled components, such as text in a disabled Button.
 */
const val DisabledAlpha = 0.38f
