package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.CheckboxDefaults.CheckboxStyle

internal object CheckboxColorTokens {
  val ContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  val OptionTextColor = ColorSchemeKeyTokens.TextPrimary
  val LabelTextColor = ColorSchemeKeyTokens.TextSecondaryTranslucent
  val DisabledOptionTextColor = ColorSchemeKeyTokens.TextDisabledTranslucent
  val DisabledLabelTextColor = ColorSchemeKeyTokens.TextDisabledTranslucent
  val ChosenIndicatorColor = ColorSchemeKeyTokens.SignalGreenElement
  val NotChosenIndicatorColor = ColorSchemeKeyTokens.FillDisabled // todo: if i put borderSecondary here as it is in figma, it's almost invisible
  val DisabledIndicatorColor = ColorSchemeKeyTokens.FillDisabledTransparent
}

internal abstract class SizeCheckboxTokens {
  abstract val TopPadding: Dp
  abstract val BottomPadding: Dp
  abstract val LabelTopPadding: Dp
  abstract val LabelBottomPadding: Dp
  abstract val IconTopPadding: Dp
  abstract val IconBottomPadding: Dp
  abstract val OptionTextFont: TypographyKeyTokens
  abstract val LabelTextFont: TypographyKeyTokens
  abstract val HorizontalPadding: Dp
  abstract val ContainerShape: ShapeKeyTokens
  abstract val IndicationShape: ShapeKeyTokens

  fun verticalPadding(style: CheckboxStyle): PaddingValues {
    val topPadding = when (style) {
      is CheckboxStyle.Label -> LabelTopPadding
      is CheckboxStyle.Icon -> IconTopPadding
      else -> TopPadding
    }
    val bottomPadding = when (style) {
      is CheckboxStyle.Label -> LabelBottomPadding
      is CheckboxStyle.Icon -> IconBottomPadding
      else -> BottomPadding
    }
    return PaddingValues(
      top = topPadding,
      bottom = bottomPadding,
    )
  }

  data object LargeSizeCheckboxTokens : SizeCheckboxTokens() {
    override val TopPadding = 16.dp
    override val BottomPadding = 18.dp
    override val LabelTopPadding = 7.dp
    override val LabelBottomPadding = 10.dp
    override val IconTopPadding = 16.dp
    override val IconBottomPadding = 16.dp
    override val OptionTextFont = TypographyKeyTokens.BodyMedium
    override val LabelTextFont = TypographyKeyTokens.Label
    override val HorizontalPadding = 16.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
    override val IndicationShape = ShapeKeyTokens.CornerSmall
  }

  data object MediumSizeCheckboxTokens : SizeCheckboxTokens() {
    override val TopPadding = 19.dp
    override val BottomPadding = 21.dp
    override val LabelTopPadding = 10.dp
    override val LabelBottomPadding = 12.dp
    override val IconTopPadding = 16.dp
    override val IconBottomPadding = 16.dp
    override val OptionTextFont = TypographyKeyTokens.BodySmall
    override val LabelTextFont = TypographyKeyTokens.Label
    override val HorizontalPadding = 16.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
    override val IndicationShape = ShapeKeyTokens.CornerSmall
  }

  data object SmallSizeCheckboxTokens : SizeCheckboxTokens() {
    override val TopPadding = 15.dp
    override val BottomPadding = 17.dp
    override val LabelTopPadding = 7.dp
    override val LabelBottomPadding = 10.dp
    override val IconTopPadding = 12.dp
    override val IconBottomPadding = 12.dp
    override val OptionTextFont = TypographyKeyTokens.BodySmall
    override val LabelTextFont = TypographyKeyTokens.FinePrint
    override val HorizontalPadding = 14.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
    override val IndicationShape = ShapeKeyTokens.CornerSmall
  }
}

internal abstract class SizeCheckboxGroupTokens {
  abstract val LabelTopPadding: Dp
  abstract val LabelBottomPadding: Dp
  abstract val HorizontalPadding: Dp
  abstract val ContainerShape: ShapeKeyTokens
  abstract val LabelTextFont: TypographyKeyTokens

  fun verticalPadding(): PaddingValues {
    return PaddingValues(
      top = LabelTopPadding,
      bottom = LabelBottomPadding,
    )
  }

  data object LargeSizeCheckboxGroupTokens : SizeCheckboxGroupTokens() {
    override val LabelTopPadding = 12.dp
    override val LabelBottomPadding = 13.dp
    override val HorizontalPadding = 16.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
    override val LabelTextFont = TypographyKeyTokens.Label
  }

  data object MediumSizeCheckboxGroupTokens : SizeCheckboxGroupTokens() {
    override val LabelTopPadding = 11.dp
    override val LabelBottomPadding = 16.dp
    override val HorizontalPadding = 16.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
    override val LabelTextFont = TypographyKeyTokens.Label
  }

  data object SmallSizeCheckboxGroupTokens : SizeCheckboxGroupTokens() {
    override val LabelTopPadding = 10.dp
    override val LabelBottomPadding = 16.dp
    override val HorizontalPadding = 14.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
    override val LabelTextFont = TypographyKeyTokens.Label
  }
}
