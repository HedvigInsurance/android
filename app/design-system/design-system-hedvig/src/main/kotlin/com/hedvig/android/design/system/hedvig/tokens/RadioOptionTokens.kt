package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle
import com.hedvig.android.design.system.hedvig.RadioOptionDefaults.RadioOptionStyle.Label

internal object RadioOptionColorTokens {
  val ContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  val OptionTextColor = ColorSchemeKeyTokens.TextPrimary
  val LabelTextColor = ColorSchemeKeyTokens.TextSecondaryTranslucent
  val DisabledOptionTextColor = ColorSchemeKeyTokens.TextDisabledTranslucent
  val DisabledLabelTextColor = ColorSchemeKeyTokens.TextDisabledTranslucent
  val ChosenIndicatorColor = ColorSchemeKeyTokens.SignalGreenElement
  val NotChosenIndicatorColor = ColorSchemeKeyTokens.BorderSecondary
  val DisabledIndicatorColor = ColorSchemeKeyTokens.FillDisabledTransparent
}

internal abstract class SizeRadioOptionTokens {
  protected abstract val TopPadding: Dp
  protected abstract val BottomPadding: Dp
  protected abstract val LabelTopPadding: Dp
  protected abstract val LabelBottomPadding: Dp
  abstract val OptionTextFont: TypographyKeyTokens
  abstract val LabelTextFont: TypographyKeyTokens
  abstract val HorizontalPadding: Dp
  abstract val ContainerShape: ShapeKeyTokens

  fun topPadding(style: RadioOptionStyle): Dp {
    return when (style) {
      Label -> LabelTopPadding
      else -> TopPadding
    }
  }

  fun bottomPadding(style: RadioOptionStyle): Dp {
    return when (style) {
      Label -> LabelBottomPadding
      else -> BottomPadding
    }
  }

  data object LargeSizeRadioOptionTokens : SizeRadioOptionTokens() {
    override val TopPadding = 16.dp
    override val BottomPadding = 18.dp
    override val LabelTopPadding = 7.dp
    override val LabelBottomPadding = 10.dp
    override val OptionTextFont = TypographyKeyTokens.BodyMedium
    override val LabelTextFont = TypographyKeyTokens.Label
    override val HorizontalPadding = 16.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
  }

  data object MediumSizeRadioOptionTokens : SizeRadioOptionTokens() {
    override val TopPadding = 19.dp
    override val BottomPadding = 21.dp
    override val LabelTopPadding = 10.dp
    override val LabelBottomPadding = 12.dp
    override val OptionTextFont = TypographyKeyTokens.BodySmall
    override val LabelTextFont = TypographyKeyTokens.Label
    override val HorizontalPadding = 16.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
  }

  data object SmallSizeRadioOptionTokens : SizeRadioOptionTokens() {
    override val TopPadding = 15.dp
    override val BottomPadding = 17.dp
    override val LabelTopPadding = 7.dp
    override val LabelBottomPadding = 10.dp
    override val OptionTextFont = TypographyKeyTokens.BodyMedium
    override val LabelTextFont = TypographyKeyTokens.FinePrint
    override val HorizontalPadding = 16.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
  }
}
