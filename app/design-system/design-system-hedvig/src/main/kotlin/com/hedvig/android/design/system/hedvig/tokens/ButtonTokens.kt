package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.dp

internal object PrimaryStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.Primary
  val ContentColor = ColorSchemeKeyTokens.Negative
  val HoverContainerColor = ColorSchemeKeyTokens.Primary
  val HoverContentColor = ColorSchemeKeyTokens.Negative
  val DisabledContainerColor = ColorSchemeKeyTokens.Disabled
  val DisabledContentColor = ColorSchemeKeyTokens.Tertiary
}

internal object PrimaryAltStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.SignalGreenFill
  val ContentColor = ColorSchemeKeyTokens.Primary
  val HoverContainerColor = ColorSchemeKeyTokens.SignalGreenHighlight
  val HoverContentColor = ColorSchemeKeyTokens.Primary
  val DisabledContainerColor = ColorSchemeKeyTokens.Disabled
  val DisabledContentColor = ColorSchemeKeyTokens.Tertiary
}

internal object SecondaryStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  val ContentColor = ColorSchemeKeyTokens.Primary
  val HoverContainerColor = ColorSchemeKeyTokens.SurfacePrimary // todo look into if this needs to be darker
  val HoverContentColor = ColorSchemeKeyTokens.Primary
  val DisabledContainerColor = ColorSchemeKeyTokens.Disabled
  val DisabledContentColor = ColorSchemeKeyTokens.Tertiary
}

internal object SecondaryAltStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.Transparent
  val ContentColor = ColorSchemeKeyTokens.Primary
  val HoverContainerColor = ColorSchemeKeyTokens.Transparent
  val HoverContentColor = ColorSchemeKeyTokens.Primary
  val DisabledContainerColor = ColorSchemeKeyTokens.Disabled
  val DisabledContentColor = ColorSchemeKeyTokens.Tertiary
}

internal object GhostStyleButtonTokens {
  val ContainerColor = ColorSchemeKeyTokens.Transparent
  val ContentColor = ColorSchemeKeyTokens.Primary
  val HoverContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  val HoverContentColor = ColorSchemeKeyTokens.Primary
  val DisabledContainerColor = ColorSchemeKeyTokens.Transparent
  val DisabledContentColor = ColorSchemeKeyTokens.Tertiary
}

internal object LargeSizeButtonTokens {
  val HorizontalPadding = 32.dp
  val TopPadding = 15.dp
  val BottomPadding = 17.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val LabelTextFont = TypographyKeyTokens.BodySmall
}

internal object MediumSizeButtonTokens {
  val HorizontalPadding = 14.dp
  val TopPadding = 7.dp
  val BottomPadding = 9.dp
  val ContainerShape = ShapeKeyTokens.CornerMedium
  val LabelTextFont = TypographyKeyTokens.BodySmall
}

internal object SmallSizeButtonTokens {
  val HorizontalPadding = 12.dp
  val TopPadding = 6.5.dp
  val BottomPadding = 7.5.dp
  val ContainerShape = ShapeKeyTokens.CornerSmall
  val LabelTextFont = TypographyKeyTokens.BodySmall
}

internal object MiniSizeButtonTokens {
  val HorizontalPadding = 8.dp
  val TopPadding = 3.dp
  val BottomPadding = 3.dp
  val ContainerShape = ShapeKeyTokens.CornerExtraSmall
  val LabelTextFont = TypographyKeyTokens.Label
}
