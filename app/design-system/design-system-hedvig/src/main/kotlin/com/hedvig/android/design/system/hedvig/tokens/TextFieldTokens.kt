package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.dp

internal object TextFieldTokens {
  val ContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  val ContainerPulsatingColor = ColorSchemeKeyTokens.SurfaceSecondary
  val TextColor = ColorSchemeKeyTokens.TextPrimary
  val TextLabelColor = ColorSchemeKeyTokens.TextSecondaryTranslucent
  val ErrorPulsatingContainerColor = ColorSchemeKeyTokens.SignalAmberFill
  val ErrorPulsatingTextColor = ColorSchemeKeyTokens.SignalAmberText
  val ErrorPulsatingTextLabelColor = ColorSchemeKeyTokens.SignalAmberText
  val XIconColor = ColorSchemeKeyTokens.FillPrimary
  val LockIconColor = ColorSchemeKeyTokens.FillSecondary
  val WarningIconColor = ColorSchemeKeyTokens.SignalAmberElement
  val BorderColor = ColorSchemeKeyTokens.BorderSecondary
  val BorderShape = ShapeKeyTokens.CornerLarge
  val BorderWidth = 0.dp
  val FocusedBorderWidth = 1.dp
  val ErrorBorderWidth = 1.dp
}

internal object LargeSizeTextFieldTokens {
  val TextStyle = TypographyKeyTokens.BodyMedium
  val LabelTextStyle = TypographyKeyTokens.Label
  val HorizontalPadding = 16.dp
  val TopPadding = 16.dp
  val BottomPadding = 18.dp
  val LabelToTextOverlap = 3.dp
  val TopPaddingWithLabel = 10.dp
  val BottomPaddingWithLabel = 9.dp
  val LabelTopPadding = 4.dp
  val LabelBottomPadding = 8.dp
  val LabelHorizontalPadding = 16.dp
}
