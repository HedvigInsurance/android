package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.dp

internal object PrimaryStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.ButtonPrimaryResting
  val ContentColor = ColorSchemeKeyTokens.TextNegative
  val HoverContainerColor = ColorSchemeKeyTokens.ButtonPrimaryHover
  val HoverContentColor = ColorSchemeKeyTokens.TextNegative
  val DisabledContainerColor = ColorSchemeKeyTokens.ButtonPrimaryDisabled
  val DisabledContentColor = ColorSchemeKeyTokens.TextTertiary
  val ActiveLoadingIndicatorColor = ColorSchemeKeyTokens.FillNegative
  val InactiveLoadingIndicatorColor = ColorSchemeKeyTokens.SurfaceSecondaryTransparent
  val RedContentColor = ColorSchemeKeyTokens.SignalRedElement
}

internal object PrimaryAltStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.ButtonPrimaryAltResting
  val ContentColor = ColorSchemeKeyTokens.TextBlack
  val HoverContainerColor = ColorSchemeKeyTokens.ButtonPrimaryAltHover
  val HoverContentColor = ColorSchemeKeyTokens.TextBlack
  val DisabledContainerColor = ColorSchemeKeyTokens.ButtonPrimaryAltDisabled
  val DisabledContentColor = ColorSchemeKeyTokens.TextTertiary
  val ActiveLoadingIndicatorColor = ColorSchemeKeyTokens.FillBlack
  val InactiveLoadingIndicatorColor = ColorSchemeKeyTokens.SurfaceSecondaryTransparent
}

internal object SecondaryStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.ButtonSecondaryResting
  val ContentColor = ColorSchemeKeyTokens.TextPrimary
  val HoverContainerColor = ColorSchemeKeyTokens.ButtonSecondaryHover
  val HoverContentColor = ColorSchemeKeyTokens.TextPrimary
  val DisabledContainerColor = ColorSchemeKeyTokens.ButtonSecondaryDisabled
  val DisabledContentColor = ColorSchemeKeyTokens.TextTertiary
  val ActiveLoadingIndicatorColor = ColorSchemeKeyTokens.FillPrimary
  val InactiveLoadingIndicatorColor = ColorSchemeKeyTokens.SurfaceSecondaryTransparent
}

internal object SecondaryAltStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.ButtonSecondaryAltResting
  val ContentColor = ColorSchemeKeyTokens.TextPrimary
  val HoverContainerColor = ColorSchemeKeyTokens.ButtonSecondaryAltHover
  val HoverContentColor = ColorSchemeKeyTokens.TextPrimary
  val DisabledContainerColor = ColorSchemeKeyTokens.ButtonSecondaryAltDisabled
  val DisabledContentColor = ColorSchemeKeyTokens.TextTertiary
  val ActiveLoadingIndicatorColor = ColorSchemeKeyTokens.FillNegative
  val InactiveLoadingIndicatorColor = ColorSchemeKeyTokens.SurfaceSecondaryTransparent
}

internal object GhostStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.ButtonGhostResting
  val ContentColor = ColorSchemeKeyTokens.TextPrimary
  val HoverContainerColor = ColorSchemeKeyTokens.ButtonGhostHover
  val HoverContentColor = ColorSchemeKeyTokens.TextPrimary
  val DisabledContainerColor = ColorSchemeKeyTokens.ButtonGhostDisabled
  val DisabledContentColor = ColorSchemeKeyTokens.TextTertiary
  val ActiveLoadingIndicatorColor = ColorSchemeKeyTokens.FillPrimary
  val InactiveLoadingIndicatorColor = ColorSchemeKeyTokens.SurfaceSecondaryTransparent
}

internal object RedStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.SignalRedElement
  val ContentColor = ColorSchemeKeyTokens.TextWhite
  val HoverContainerColor = ColorSchemeKeyTokens.ButtonPrimaryHover
  val HoverContentColor = ColorSchemeKeyTokens.TextWhite
  val DisabledContainerColor = ColorSchemeKeyTokens.ButtonPrimaryDisabled
  val DisabledContentColor = ColorSchemeKeyTokens.TextTertiary
  val ActiveLoadingIndicatorColor = ColorSchemeKeyTokens.TextWhite
  val InactiveLoadingIndicatorColor = ColorSchemeKeyTokens.SignalRedText
  val RedContentColor = ColorSchemeKeyTokens.SignalRedElement // should not be used ever
}

internal object RoundedPrimaryStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.ButtonRoundedPrimaryResting
  val ContentColor = ColorSchemeKeyTokens.ButtonRoundedPrimaryContent
  val HoverContainerColor = ColorSchemeKeyTokens.ButtonRoundedPrimaryHover
  val HoverContentColor = ColorSchemeKeyTokens.ButtonRoundedPrimaryHoverContent
  val DisabledContainerColor = ColorSchemeKeyTokens.ButtonRoundedPrimaryDisabled
  val DisabledContentColor = ColorSchemeKeyTokens.ButtonRoundedPrimaryDisabledContent
  val ActiveLoadingIndicatorColor = ColorSchemeKeyTokens.ButtonRoundedPrimaryLoadingActive
  val InactiveLoadingIndicatorColor = ColorSchemeKeyTokens.ButtonRoundedPrimaryLoadingInactive
  val RedContentColor = ColorSchemeKeyTokens.ButtonRoundedPrimaryRedContent
}

internal object RoundedLiquidGlassStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.ButtonRoundedLiquidGlassResting
  val ContentColor = ColorSchemeKeyTokens.ButtonRoundedLiquidGlassContent
  val HoverContainerColor = ColorSchemeKeyTokens.ButtonRoundedLiquidGlassHover
  val HoverContentColor = ColorSchemeKeyTokens.ButtonRoundedLiquidGlassHoverContent
  val DisabledContainerColor = ColorSchemeKeyTokens.ButtonRoundedLiquidGlassDisabled
  val DisabledContentColor = ColorSchemeKeyTokens.ButtonRoundedLiquidGlassDisabledContent
  val ActiveLoadingIndicatorColor = ColorSchemeKeyTokens.ButtonRoundedLiquidGlassLoadingActive
  val InactiveLoadingIndicatorColor = ColorSchemeKeyTokens.ButtonRoundedLiquidGlassLoadingInactive
  val RedContentColor = ColorSchemeKeyTokens.ButtonRoundedLiquidGlassRedContent
}

internal object LargeSizeButtonTokens {
  val HorizontalPadding = 32.dp
  val TopPadding = 15.dp
  val BottomPadding = 17.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val LabelTextFont = TypographyKeyTokens.BodySmall
}

// TODO: the rounded button styles are only designed at the large size. Every smaller size falls back to
//  the standard button metrics above until design provides them.
internal object RoundedLargeSizeButtonTokens {
  val HorizontalPadding = 20.dp
  val TopPadding = 12.dp
  val BottomPadding = 12.dp
  val ContainerShape = ShapeKeyTokens.CornerFull
  val LabelTextFont = TypographyKeyTokens.BodySmall
}

internal object MediumSizeButtonTokens {
  val HorizontalPadding = 14.dp
  val TopPadding = 7.dp // todo look into if we use the right line heights to justify the offset introduced here
  val BottomPadding = 9.dp
  val ContainerShape = ShapeKeyTokens.CornerMedium
  val LabelTextFont = TypographyKeyTokens.BodySmall
}

internal object SmallSizeButtonTokens {
  val HorizontalPadding = 12.dp
  val TopPadding = 6.5.dp
  val BottomPadding = 7.5.dp
  val ContainerShape = ShapeKeyTokens.CornerSmall
  val LabelTextFont = TypographyKeyTokens.Label
}

internal object MiniSizeButtonTokens {
  val HorizontalPadding = 8.dp
  val TopPadding = 3.dp
  val BottomPadding = 3.dp
  val ContainerShape = ShapeKeyTokens.CornerXSmall
  val LabelTextFont = TypographyKeyTokens.Label
}
