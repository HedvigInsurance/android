package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import com.hedvig.android.design.system.hedvig.tokens.ColorLightTokens
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens

@Immutable
data class ColorScheme(
  val textPrimary: Color,
  val textNegative: Color,
  val textSecondary: Color,
  val textAccordion: Color,
  val textTertiary: Color,
  val textDisabled: Color,
  val textBlack: Color,
  val textWhite: Color,
  val action: Color,
  val link: Color,
  val buttonPrimaryResting: Color,
  val buttonPrimaryHover: Color,
  val buttonPrimaryDisabled: Color,
  val buttonPrimaryAltResting: Color,
  val buttonPrimaryAltHover: Color,
  val buttonPrimaryAltDisabled: Color,
  val buttonSecondaryResting: Color,
  val buttonSecondaryHover: Color,
  val buttonSecondaryDisabled: Color,
  val buttonSecondaryAltResting: Color,
  val buttonSecondaryAltHover: Color,
  val buttonSecondaryAltDisabled: Color,
  val buttonGhostResting: Color,
  val buttonGhostHover: Color,
  val buttonGhostDisabled: Color,
  val fillPrimary: Color,
  val fillSecondary: Color,
  val fillTertiary: Color,
  val fillDisabled: Color,
  val fillNegative: Color,
  val fillBlack: Color,
  val fillWhite: Color,
  val fillPrimaryTransparent: Color,
  val fillSecondaryTransparent: Color,
  val fillTertiaryTransparent: Color,
  val fillDisabledTransparent: Color,
  val fillNegativeTransparent: Color,
  val fillBlackTransparent: Color,
  val fillWhiteTransparent: Color,
  val surfacePrimary: Color,
  val surfaceSecondary: Color,
  val surfacePrimaryTransparent: Color,
  val surfaceSecondaryTransparent: Color,
  val backgroundPrimary: Color,
  val backgroundNegative: Color,
  val backgroundBlack: Color,
  val backgroundWhite: Color,
  val borderPrimary: Color,
  val borderSecondary: Color,
  val borderHighlight: Color,
  val signalRedFill: Color,
  val signalRedHighlight: Color,
  val signalRedElement: Color,
  val signalRedText: Color,
  val signalAmberFill: Color,
  val signalAmberHighlight: Color,
  val signalAmberElement: Color,
  val signalAmberText: Color,
  val signalGreenFill: Color,
  val signalGreenHighlight: Color,
  val signalGreenElement: Color,
  val signalGreenText: Color,
  val signalBlueFill: Color,
  val signalBlueHighlight: Color,
  val signalBlueElement: Color,
  val signalBlueText: Color,
  val highlightPinkFill1: Color,
  val highlightPinkFill2: Color,
  val highlightPinkFill3: Color,
  val highlightYellowFill1: Color,
  val highlightYellowFill2: Color,
  val highlightYellowFill3: Color,
  val highlightGreenFill1: Color,
  val highlightGreenFill2: Color,
  val highlightGreenFill3: Color,
  val highlightTealFill1: Color,
  val highlightTealFill2: Color,
  val highlightTealFill3: Color,
  val highlightBlueFill1: Color,
  val highlightBlueFill2: Color,
  val highlightBlueFill3: Color,
  val highlightPurpleFill1: Color,
  val highlightPurpleFill2: Color,
  val highlightPurpleFill3: Color,
  val transparent: Color,
)

internal val lightColorScheme: ColorScheme = ColorScheme(
  textPrimary = ColorLightTokens.TextPrimary,
  textNegative = ColorLightTokens.TextNegative,
  textSecondary = ColorLightTokens.TextSecondary,
  textAccordion = ColorLightTokens.TextAccordion,
  textTertiary = ColorLightTokens.TextTertiary,
  textDisabled = ColorLightTokens.TextDisabled,
  textBlack = ColorLightTokens.TextBlack,
  textWhite = ColorLightTokens.TextWhite,
  action = ColorLightTokens.Action,
  link = ColorLightTokens.Link,
  buttonPrimaryResting = ColorLightTokens.ButtonPrimaryResting,
  buttonPrimaryHover = ColorLightTokens.ButtonPrimaryHover,
  buttonPrimaryDisabled = ColorLightTokens.ButtonPrimaryDisabled,
  buttonPrimaryAltResting = ColorLightTokens.ButtonPrimaryAltResting,
  buttonPrimaryAltHover = ColorLightTokens.ButtonPrimaryAltHover,
  buttonPrimaryAltDisabled = ColorLightTokens.ButtonPrimaryAltDisabled,
  buttonSecondaryResting = ColorLightTokens.ButtonSecondaryResting,
  buttonSecondaryHover = ColorLightTokens.ButtonSecondaryHover,
  buttonSecondaryDisabled = ColorLightTokens.ButtonSecondaryDisabled,
  buttonSecondaryAltResting = ColorLightTokens.ButtonSecondaryAltResting,
  buttonSecondaryAltHover = ColorLightTokens.ButtonSecondaryAltHover,
  buttonSecondaryAltDisabled = ColorLightTokens.ButtonSecondaryAltDisabled,
  buttonGhostResting = ColorLightTokens.ButtonGhostResting,
  buttonGhostHover = ColorLightTokens.ButtonGhostHover,
  buttonGhostDisabled = ColorLightTokens.ButtonGhostDisabled,
  fillPrimary = ColorLightTokens.FillPrimary,
  fillSecondary = ColorLightTokens.FillSecondary,
  fillTertiary = ColorLightTokens.FillTertiary,
  fillDisabled = ColorLightTokens.FillDisabled,
  fillNegative = ColorLightTokens.FillNegative,
  fillBlack = ColorLightTokens.FillBlack,
  fillWhite = ColorLightTokens.FillWhite,
  fillPrimaryTransparent = ColorLightTokens.FillPrimaryTransparent,
  fillSecondaryTransparent = ColorLightTokens.FillSecondaryTransparent,
  fillTertiaryTransparent = ColorLightTokens.FillTertiaryTransparent,
  fillDisabledTransparent = ColorLightTokens.FillDisabledTransparent,
  fillNegativeTransparent = ColorLightTokens.FillNegativeTransparent,
  fillBlackTransparent = ColorLightTokens.FillBlackTransparent,
  fillWhiteTransparent = ColorLightTokens.FillWhiteTransparent,
  surfacePrimary = ColorLightTokens.SurfacePrimary,
  surfaceSecondary = ColorLightTokens.SurfaceSecondary,
  surfacePrimaryTransparent = ColorLightTokens.SurfacePrimaryTransparent,
  surfaceSecondaryTransparent = ColorLightTokens.SurfaceSecondaryTransparent,
  backgroundPrimary = ColorLightTokens.BackgroundPrimary,
  backgroundNegative = ColorLightTokens.BackgroundNegative,
  backgroundBlack = ColorLightTokens.BackgroundBlack,
  backgroundWhite = ColorLightTokens.BackgroundWhite,
  borderPrimary = ColorLightTokens.BorderPrimary,
  borderSecondary = ColorLightTokens.BorderSecondary,
  borderHighlight = ColorLightTokens.BorderHighlight,
  signalRedFill = ColorLightTokens.SignalRedFill,
  signalRedHighlight = ColorLightTokens.SignalRedHighlight,
  signalRedElement = ColorLightTokens.SignalRedElement,
  signalRedText = ColorLightTokens.SignalRedText,
  signalAmberFill = ColorLightTokens.SignalAmberFill,
  signalAmberHighlight = ColorLightTokens.SignalAmberHighlight,
  signalAmberElement = ColorLightTokens.SignalAmberElement,
  signalAmberText = ColorLightTokens.SignalAmberText,
  signalGreenFill = ColorLightTokens.SignalGreenFill,
  signalGreenHighlight = ColorLightTokens.SignalGreenHighlight,
  signalGreenElement = ColorLightTokens.SignalGreenElement,
  signalGreenText = ColorLightTokens.SignalGreenText,
  signalBlueFill = ColorLightTokens.SignalBlueFill,
  signalBlueHighlight = ColorLightTokens.SignalBlueHighlight,
  signalBlueElement = ColorLightTokens.SignalBlueElement,
  signalBlueText = ColorLightTokens.SignalBlueText,
  highlightPinkFill1 = ColorLightTokens.HighlightPinkFill1,
  highlightPinkFill2 = ColorLightTokens.HighlightPinkFill2,
  highlightPinkFill3 = ColorLightTokens.HighlightPinkFill3,
  highlightYellowFill1 = ColorLightTokens.HighlightYellowFill1,
  highlightYellowFill2 = ColorLightTokens.HighlightYellowFill2,
  highlightYellowFill3 = ColorLightTokens.HighlightYellowFill3,
  highlightGreenFill1 = ColorLightTokens.HighlightGreenFill1,
  highlightGreenFill2 = ColorLightTokens.HighlightGreenFill2,
  highlightGreenFill3 = ColorLightTokens.HighlightGreenFill3,
  highlightTealFill1 = ColorLightTokens.HighlightTealFill1,
  highlightTealFill2 = ColorLightTokens.HighlightTealFill2,
  highlightTealFill3 = ColorLightTokens.HighlightTealFill3,
  highlightBlueFill1 = ColorLightTokens.HighlightBlueFill1,
  highlightBlueFill2 = ColorLightTokens.HighlightBlueFill2,
  highlightBlueFill3 = ColorLightTokens.HighlightBlueFill3,
  highlightPurpleFill1 = ColorLightTokens.HighlightPurpleFill1,
  highlightPurpleFill2 = ColorLightTokens.HighlightPurpleFill2,
  highlightPurpleFill3 = ColorLightTokens.HighlightPurpleFill3,
  transparent = ColorLightTokens.Transparent,
)

internal val darkColorScheme: ColorScheme = lightColorScheme
//  ColorScheme() todo dark mode

@Stable
internal fun ColorScheme.contentColorFor(backgroundColor: Color): Color {
  // todo more colors here, and check if this makes sense at all for us here
  return when (backgroundColor) {
    else -> Color.Unspecified
  }
}

@Composable
@ReadOnlyComposable
fun contentColorFor(backgroundColor: Color): Color {
  return HedvigTheme.colorScheme.contentColorFor(backgroundColor).takeOrElse {
    LocalContentColor.current
  }
}

@Stable
internal fun ColorScheme.fromToken(value: ColorSchemeKeyTokens): Color {
  return when (value) {
    ColorSchemeKeyTokens.TextPrimary -> textPrimary
    ColorSchemeKeyTokens.TextNegative -> textNegative
    ColorSchemeKeyTokens.TextSecondary -> textSecondary
    ColorSchemeKeyTokens.TextAccordion -> textAccordion
    ColorSchemeKeyTokens.TextTertiary -> textTertiary
    ColorSchemeKeyTokens.TextDisabled -> textDisabled
    ColorSchemeKeyTokens.TextBlack -> textBlack
    ColorSchemeKeyTokens.TextWhite -> textWhite
    ColorSchemeKeyTokens.Action -> action
    ColorSchemeKeyTokens.Link -> link
    ColorSchemeKeyTokens.ButtonPrimaryResting -> buttonPrimaryResting
    ColorSchemeKeyTokens.ButtonPrimaryHover -> buttonPrimaryHover
    ColorSchemeKeyTokens.ButtonPrimaryDisabled -> buttonPrimaryDisabled
    ColorSchemeKeyTokens.ButtonPrimaryAltResting -> buttonPrimaryAltResting
    ColorSchemeKeyTokens.ButtonPrimaryAltHover -> buttonPrimaryAltHover
    ColorSchemeKeyTokens.ButtonPrimaryAltDisabled -> buttonPrimaryAltDisabled
    ColorSchemeKeyTokens.ButtonSecondaryResting -> buttonSecondaryResting
    ColorSchemeKeyTokens.ButtonSecondaryHover -> buttonSecondaryHover
    ColorSchemeKeyTokens.ButtonSecondaryDisabled -> buttonSecondaryDisabled
    ColorSchemeKeyTokens.ButtonSecondaryAltResting -> buttonSecondaryAltResting
    ColorSchemeKeyTokens.ButtonSecondaryAltHover -> buttonSecondaryAltHover
    ColorSchemeKeyTokens.ButtonSecondaryAltDisabled -> buttonSecondaryAltDisabled
    ColorSchemeKeyTokens.ButtonGhostResting -> buttonGhostResting
    ColorSchemeKeyTokens.ButtonGhostHover -> buttonGhostHover
    ColorSchemeKeyTokens.ButtonGhostDisabled -> buttonGhostDisabled
    ColorSchemeKeyTokens.FillPrimary -> fillPrimary
    ColorSchemeKeyTokens.FillSecondary -> fillSecondary
    ColorSchemeKeyTokens.FillTertiary -> fillTertiary
    ColorSchemeKeyTokens.FillDisabled -> fillDisabled
    ColorSchemeKeyTokens.FillNegative -> fillNegative
    ColorSchemeKeyTokens.FillBlack -> fillBlack
    ColorSchemeKeyTokens.FillWhite -> fillWhite
    ColorSchemeKeyTokens.FillPrimaryTransparent -> fillPrimaryTransparent
    ColorSchemeKeyTokens.FillSecondaryTransparent -> fillSecondaryTransparent
    ColorSchemeKeyTokens.FillTertiaryTransparent -> fillTertiaryTransparent
    ColorSchemeKeyTokens.FillDisabledTransparent -> fillDisabledTransparent
    ColorSchemeKeyTokens.FillNegativeTransparent -> fillNegativeTransparent
    ColorSchemeKeyTokens.FillBlackTransparent -> fillBlackTransparent
    ColorSchemeKeyTokens.FillWhiteTransparent -> fillWhiteTransparent
    ColorSchemeKeyTokens.SurfacePrimary -> surfacePrimary
    ColorSchemeKeyTokens.SurfaceSecondary -> surfaceSecondary
    ColorSchemeKeyTokens.SurfacePrimaryTransparent -> surfacePrimaryTransparent
    ColorSchemeKeyTokens.SurfaceSecondaryTransparent -> surfaceSecondaryTransparent
    ColorSchemeKeyTokens.BackgroundPrimary -> backgroundPrimary
    ColorSchemeKeyTokens.BackgroundNegative -> backgroundNegative
    ColorSchemeKeyTokens.BackgroundBlack -> backgroundBlack
    ColorSchemeKeyTokens.BackgroundWhite -> backgroundWhite
    ColorSchemeKeyTokens.BorderPrimary -> borderPrimary
    ColorSchemeKeyTokens.BorderSecondary -> borderSecondary
    ColorSchemeKeyTokens.BorderHighlight -> borderHighlight
    ColorSchemeKeyTokens.SignalRedFill -> signalRedFill
    ColorSchemeKeyTokens.SignalRedHighlight -> signalRedHighlight
    ColorSchemeKeyTokens.SignalRedElement -> signalRedElement
    ColorSchemeKeyTokens.SignalRedText -> signalRedText
    ColorSchemeKeyTokens.SignalAmberFill -> signalAmberFill
    ColorSchemeKeyTokens.SignalAmberHighlight -> signalAmberHighlight
    ColorSchemeKeyTokens.SignalAmberElement -> signalAmberElement
    ColorSchemeKeyTokens.SignalAmberText -> signalAmberText
    ColorSchemeKeyTokens.SignalGreenFill -> signalGreenFill
    ColorSchemeKeyTokens.SignalGreenHighlight -> signalGreenHighlight
    ColorSchemeKeyTokens.SignalGreenElement -> signalGreenElement
    ColorSchemeKeyTokens.SignalGreenText -> signalGreenText
    ColorSchemeKeyTokens.SignalBlueFill -> signalBlueFill
    ColorSchemeKeyTokens.SignalBlueHighlight -> signalBlueHighlight
    ColorSchemeKeyTokens.SignalBlueElement -> signalBlueElement
    ColorSchemeKeyTokens.SignalBlueText -> signalBlueText
    ColorSchemeKeyTokens.HighlightPinkFill1 -> highlightPinkFill1
    ColorSchemeKeyTokens.HighlightPinkFill2 -> highlightPinkFill2
    ColorSchemeKeyTokens.HighlightPinkFill3 -> highlightPinkFill3
    ColorSchemeKeyTokens.HighlightYellowFill1 -> highlightYellowFill1
    ColorSchemeKeyTokens.HighlightYellowFill2 -> highlightYellowFill2
    ColorSchemeKeyTokens.HighlightYellowFill3 -> highlightYellowFill3
    ColorSchemeKeyTokens.HighlightGreenFill1 -> highlightGreenFill1
    ColorSchemeKeyTokens.HighlightGreenFill2 -> highlightGreenFill2
    ColorSchemeKeyTokens.HighlightGreenFill3 -> highlightGreenFill3
    ColorSchemeKeyTokens.HighlightTealFill1 -> highlightTealFill1
    ColorSchemeKeyTokens.HighlightTealFill2 -> highlightTealFill2
    ColorSchemeKeyTokens.HighlightTealFill3 -> highlightTealFill3
    ColorSchemeKeyTokens.HighlightBlueFill1 -> highlightBlueFill1
    ColorSchemeKeyTokens.HighlightBlueFill2 -> highlightBlueFill2
    ColorSchemeKeyTokens.HighlightBlueFill3 -> highlightBlueFill3
    ColorSchemeKeyTokens.HighlightPurpleFill1 -> highlightPurpleFill1
    ColorSchemeKeyTokens.HighlightPurpleFill2 -> highlightPurpleFill2
    ColorSchemeKeyTokens.HighlightPurpleFill3 -> highlightPurpleFill3
    ColorSchemeKeyTokens.Transparent -> transparent
  }
}

internal val ColorSchemeKeyTokens.value: Color
  @ReadOnlyComposable
  @Composable
  get() = HedvigTheme.colorScheme.fromToken(this)

internal val LocalColorScheme = staticCompositionLocalOf { lightColorScheme }
