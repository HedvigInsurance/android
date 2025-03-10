package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.dp

internal object DropdownTokens {
//  val ContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  // todo: Temp while this is still figured out and we get a new translucent or better contrast surface color
  //  https://hedviginsurance.slack.com/archives/C03U9C6Q7TP/p1729261859881029?thread_ts=1729253107.272419&cid=C03U9C6Q7TP
  val ContainerColor = ColorSchemeKeyTokens.ButtonSecondaryResting
  val EnabledChevronColor = ColorSchemeKeyTokens.FillPrimary

  // todo: disabled - temp color while this is still figured out (bc of ContainerColor above)
  val DisabledChevronColor = ColorSchemeKeyTokens.TextSecondary

  // val DisabledChevronColor = ColorSchemeKeyTokens.FillDisabled
  val TextColor = ColorSchemeKeyTokens.TextPrimary

  // todo: Temp while this is still figured out (bc of ContainerColor above)
  val DisabledTextColor = ColorSchemeKeyTokens.TextSecondary

  // val DisabledTextColor = ColorSchemeKeyTokens.TextDisabled
  val LabelColor = ColorSchemeKeyTokens.TextSecondary
  val HintColor = ColorSchemeKeyTokens.TextSecondary
  val PulsatingContainerColor = ColorSchemeKeyTokens.SignalAmberFill
  val PulsatingContentColor = ColorSchemeKeyTokens.SignalAmberText
  val ErrorIconColor = ColorSchemeKeyTokens.SignalAmberElement
  val ErrorTextColor = ColorSchemeKeyTokens.TextSecondary
  val PulsatingChevronColor = ColorSchemeKeyTokens.FillBlack
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val ErrorTextFont = TypographyKeyTokens.Label
  val ChevronSize = 24.dp
  val IconSize = 32.dp
}

internal object CommonLargeDropdownTokens {
  val TextFont = TypographyKeyTokens.BodyMedium
  val ErrorTextHorizontalPadding = 16.dp
}

internal object CommonMediumDropdownTokens {
  val TextFont = TypographyKeyTokens.BodySmall
  val ErrorTextHorizontalPadding = 16.dp
}

internal object CommonSmallDropdownTokens {
  val TextFont = TypographyKeyTokens.BodySmall
  val ErrorTextStartPadding = 14.dp
  val ErrorTextEndPadding = 16.dp
}

internal object LargeSizeDefaultDropdownTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 16.dp
  val BottomPadding = 18.dp
}

internal object MediumSizeDefaultDropdownTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 16.dp
  val BottomPadding = 18.dp
}

internal object SmallSizeDefaultDropdownTokens {
  val HorizontalPadding = 14.dp
  val TopPadding = 15.dp
  val BottomPadding = 17.dp
}

internal object LargeSizeIconDropdownTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 16.dp
  val BottomPadding = 18.dp
}

internal object MediumSizeIconDropdownTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 16.dp
  val BottomPadding = 18.dp
}

internal object SmallSizeIconDropdownTokens {
  val HorizontalPadding = 14.dp
  val TopPadding = 12.dp
  val BottomPadding = 12.dp
}

internal object LargeSizeLabeledDropdownTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 10.dp
  val BottomPadding = 9.dp
  val LabelTextFont = TypographyKeyTokens.Label
}

internal object MediumSizeLabeledDropdownTokens {
  val HorizontalPadding = 16.dp
  val TopPadding = 11.5.dp
  val BottomPadding = 12.5.dp
  val LabelTextFont = TypographyKeyTokens.Label
}

internal object SmallSizeLabeledDropdownTokens {
  val HorizontalPadding = 14.dp
  val TopPadding = 8.5.dp
  val BottomPadding = 7.5.dp
  val LabelTextFont = TypographyKeyTokens.Label
}
