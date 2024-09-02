package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.dp

internal object DropdownTokens {
  val ContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  val EnabledChevronColor = ColorSchemeKeyTokens.FillPrimary
  val DisabledChevronColor = ColorSchemeKeyTokens.FillDisabled
  val TextColor = ColorSchemeKeyTokens.TextPrimary
  val DisabledTextColor = ColorSchemeKeyTokens.TextDisabled
  val LabelColor = ColorSchemeKeyTokens.TextSecondary
  val HintColor = ColorSchemeKeyTokens.TextSecondary
  val DisabledLabelColor = ColorSchemeKeyTokens.TextDisabled
  val PulsatingContainerColor = ColorSchemeKeyTokens.SignalAmberFill
  val PulsatingContentColor = ColorSchemeKeyTokens.SignalAmberText
  val ErrorIconColor = ColorSchemeKeyTokens.SignalAmberElement
  val ErrorTextColor = ColorSchemeKeyTokens.TextSecondary
  val PulsatingChevronColor = ColorSchemeKeyTokens.FillBlack
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val ErrorTextFont = TypographyKeyTokens.Label
  val ChevronSize = 24.dp
}

internal object LargeSizeDefaultDropdownTokens {//TODO - check them all!
  val HorizontalPadding = 16.dp
  val TopPadding = 16.dp
  val BottomPadding = 18.dp
  val TextFont = TypographyKeyTokens.BodyMedium
  val ErrorTextStartPadding = 16.dp
  val ErrorTextEndPadding = 16.dp

  //val MinHeight = 64.dp //todo: remove?
}

internal object MediumSizeDefaultDropdownTokens {//TODO - check them all!
  val HorizontalPadding = 16.dp
  val TopPadding = 16.dp
  val BottomPadding = 18.dp

  val TextFont = TypographyKeyTokens.BodySmall
  val ErrorTextStartPadding = 14.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 64.dp
}

internal object SmallSizeDefaultDropdownTokens {//TODO - check them all!
  val HorizontalPadding = 14.dp
  val TopPadding = 15.dp
  val BottomPadding = 17.dp

  val DropdownTopPadding = 0.dp
  val DropdownBottomPadding = 0.dp
  val TextFont = TypographyKeyTokens.BodySmall
  val ErrorTextStartPadding = 14.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 56.dp
}

internal object LargeSizeLabeledDropdownTokens {//TODO - check them all!
  val HorizontalPadding = 16.dp
  val TopPadding = 10.dp
  val BottomPadding = 9.dp
  val LabelTextFont = TypographyKeyTokens.Label
  val TextFont = TypographyKeyTokens.BodyMedium
  val ErrorTextStartPadding = 16.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 64.dp
}

internal object MediumSizeLabeledDropdownTokens {//TODO - check them all!
  val HorizontalPadding = 16.dp
  val TopPadding = 11.5.dp
  val BottomPadding = 12.5.dp
  val LabelTextFont = TypographyKeyTokens.Label
  val TextFont = TypographyKeyTokens.BodySmall
  val ErrorTextStartPadding = 14.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 64.dp
}

internal object SmallSizeLabeledDropdownTokens {//TODO - check them all!
  val HorizontalPadding = 14.dp
  val TopPadding = 8.5.dp
  val BottomPadding = 7.5.dp
  val LabelTextFont = TypographyKeyTokens.Label
  val TextFont = TypographyKeyTokens.BodySmall
  val ErrorTextStartPadding = 14.dp
  val ErrorTextEndPadding = 16.dp
  val MinHeight = 56.dp
}
