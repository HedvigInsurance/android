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
}

internal object LargeSizeDefaultStepperTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 9.dp
  val BottomPadding = 10.dp
  val StepperTopPadding = 0.dp
  val StepperBottomPadding = 0.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val TextFont = TypographyKeyTokens.BodyMedium
  val ErrorTextStartPadding = 16.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 64.dp
}

internal object MediumSizeDefaultStepperTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 9.dp
  val BottomPadding = 10.dp
  val StepperTopPadding = 0.dp
  val StepperBottomPadding = 0.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val TextFont = TypographyKeyTokens.BodySmall
  val ErrorTextStartPadding = 14.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 64.dp
}

internal object SmallSizeDefaultStepperTokens {
  val HorizontalPadding = 14.dp
  val TopPadding = 5.dp
  val BottomPadding = 6.dp
  val StepperTopPadding = 0.dp
  val StepperBottomPadding = 0.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val TextFont = TypographyKeyTokens.BodySmall
  val ErrorTextStartPadding = 14.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 56.dp
}

internal object LargeSizeLabeledStepperTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 9.dp
  val BottomPadding = 10.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val LabelTextFont = TypographyKeyTokens.Label
  val TextFont = TypographyKeyTokens.BodyMedium
  val ErrorTextStartPadding = 16.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 64.dp
}

internal object MediumSizeLabeledStepperTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 9.dp
  val BottomPadding = 10.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val LabelTextFont = TypographyKeyTokens.Label
  val TextFont = TypographyKeyTokens.BodySmall
  val ErrorTextStartPadding = 14.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 64.dp
}

internal object SmallSizeLabeledStepperTokens {
  val HorizontalPadding = 14.dp
  val TopPadding = 5.dp
  val BottomPadding = 6.dp
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val LabelTextFont = TypographyKeyTokens.Label
  val TextFont = TypographyKeyTokens.BodySmall
  val ErrorTextStartPadding = 14.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 56.dp
}
