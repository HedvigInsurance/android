package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.dp

internal object StepperColorTokens {
  val ContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  val EnabledSymbolColor = ColorSchemeKeyTokens.FillPrimary
  val DisabledSymbolColor = ColorSchemeKeyTokens.FillDisabled
  val TextColor = ColorSchemeKeyTokens.TextPrimary
  val LabelColor = ColorSchemeKeyTokens.TextSecondaryTranslucent // todo: should we use Translucent?
  val PulsatingContainerColor = ColorSchemeKeyTokens.SignalAmberFill
  val PulsatingContentColor = ColorSchemeKeyTokens.SignalAmberText
  val ErrorDescriptionColor = ColorSchemeKeyTokens.TextSecondaryTranslucent // todo: should we use Translucent?
}

internal object LargeSizeDefaultStepperTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 16.dp
  val BottomPadding = 18.dp
  val StepperTopPadding = 0.dp
  val StepperBottomPadding = 0.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val TextFont = TypographyKeyTokens.BodyMedium
}

internal object MediumSizeDefaultStepperTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 16.dp
  val BottomPadding = 18.dp
  val StepperTopPadding = 4.dp
  val StepperBottomPadding = 2.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val TextFont = TypographyKeyTokens.BodySmall
}

internal object SmallSizeDefaultStepperTokens {
  val HorizontalPadding = 14.dp
  val TopPadding = 15.dp
  val BottomPadding = 17.dp
  val StepperTopPadding = 0.dp
  val StepperBottomPadding = 0.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val TextFont = TypographyKeyTokens.BodySmall
}

internal object LargeSizeLabeledStepperTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 10.dp
  val BottomPadding = 9.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val LabelTextFont = TypographyKeyTokens.Label
  val TextFont = TypographyKeyTokens.BodyMedium
}

internal object MediumSizeLabeledStepperTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 11.dp
  val BottomPadding = 12.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val LabelTextFont = TypographyKeyTokens.Label
  val TextFont = TypographyKeyTokens.BodySmall
}

internal object SmallSizeLabeledStepperTokens {
  val HorizontalPadding = 14.dp
  val TopPadding = 7.dp
  val BottomPadding = 8.dp // todo: reversed?
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val LabelTextFont = TypographyKeyTokens.Label
  val TextFont = TypographyKeyTokens.BodySmall
}
