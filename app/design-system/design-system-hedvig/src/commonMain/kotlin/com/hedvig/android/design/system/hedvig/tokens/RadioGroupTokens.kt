package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object RadioGroupColorTokens {
  val ContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  val OptionTextColor = ColorSchemeKeyTokens.TextPrimary
  val LabelTextColor = ColorSchemeKeyTokens.TextSecondaryTranslucent
  val DisabledOptionTextColor = ColorSchemeKeyTokens.TextDisabledTranslucent
  val DisabledLabelTextColor = ColorSchemeKeyTokens.TextDisabledTranslucent
  val DividerColor = ColorSchemeKeyTokens.BorderPrimary
  val IndicatorColor = ColorSchemeKeyTokens.BorderSecondary
  val IndicatorSelectedColor = ColorSchemeKeyTokens.SignalGreenElement
  val IndicatorDisabledColor = ColorSchemeKeyTokens.FillDisabled
}

internal object RadioGroupStyleTokens {
  val FlowLabelSpacing: Dp = 16.dp
  val VerticalItemSpacing: Dp = 4.dp
  val HorizontalItemSpacing: Dp = 8.dp
  val TextToLabelSpacing: Dp = 4.dp
  val IndicatorSize: Dp = 24.dp
  val ContainerShape: ShapeKeyTokens = ShapeKeyTokens.CornerLarge
}

@Suppress("PropertyName")
internal sealed interface RadioGroupSizeTokens {
  val TopPadding: Dp
  val BottomPadding: Dp
  val HorizontalPadding: Dp
  val LabeledTopPadding: Dp
  val LabeledBottomPadding: Dp
  val LabeledHorizontalPadding: Dp
  val LabelTopPadding: Dp
  val LabelHorizontalPadding: Dp
  val TextStyle: TypographyKeyTokens
  val TextStyleLabel: TypographyKeyTokens

  object Large : RadioGroupSizeTokens {
    override val TopPadding: Dp = 16.dp
    override val BottomPadding: Dp = 18.dp
    override val HorizontalPadding: Dp = 16.dp
    override val LabeledTopPadding: Dp = 7.dp
    override val LabeledBottomPadding: Dp = 10.dp
    override val LabeledHorizontalPadding: Dp = 16.dp
    override val LabelTopPadding: Dp = 12.dp
    override val LabelHorizontalPadding: Dp = 16.dp
    override val TextStyle: TypographyKeyTokens = TypographyKeyTokens.BodyMedium
    override val TextStyleLabel: TypographyKeyTokens = TypographyKeyTokens.Label
  }

  object Medium : RadioGroupSizeTokens {
    override val TopPadding: Dp = 16.dp
    override val BottomPadding: Dp = 18.dp
    override val HorizontalPadding: Dp = 16.dp
    override val LabeledTopPadding: Dp = 10.dp
    override val LabeledBottomPadding: Dp = 12.dp
    override val LabeledHorizontalPadding: Dp = 16.dp
    override val LabelTopPadding: Dp = 12.dp
    override val LabelHorizontalPadding: Dp = 16.dp
    override val TextStyle: TypographyKeyTokens = TypographyKeyTokens.BodySmall
    override val TextStyleLabel: TypographyKeyTokens = TypographyKeyTokens.Label
  }

  object Small : RadioGroupSizeTokens {
    override val TopPadding: Dp = 15.dp
    override val BottomPadding: Dp = 17.dp
    override val HorizontalPadding: Dp = 14.dp
    override val LabeledTopPadding: Dp = 14.dp
    override val LabeledBottomPadding: Dp = 17.dp
    override val LabeledHorizontalPadding: Dp = 14.dp
    override val LabelTopPadding: Dp = 10.dp
    override val LabelHorizontalPadding: Dp = 14.dp
    override val TextStyle: TypographyKeyTokens = TypographyKeyTokens.BodySmall
    override val TextStyleLabel: TypographyKeyTokens = TypographyKeyTokens.Label
  }
}
