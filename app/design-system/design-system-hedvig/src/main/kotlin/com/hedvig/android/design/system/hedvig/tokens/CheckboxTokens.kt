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
  val NotChosenIndicatorColor = ColorSchemeKeyTokens.BorderSecondary
  val DisabledIndicatorColor = ColorSchemeKeyTokens.FillDisabledTransparent
}

internal abstract class SizeCheckboxTokens {
  abstract val TopPadding: Dp
  abstract val BottomPadding: Dp
  abstract val LabelTopPadding: Dp
  abstract val LabelBottomPadding: Dp
  abstract val OptionTextFont: TypographyKeyTokens
  abstract val LabelTextFont: TypographyKeyTokens
  abstract val HorizontalPadding: Dp
  abstract val ContainerShape: ShapeKeyTokens

  fun verticalPadding(style: CheckboxStyle): PaddingValues {
    val topPadding = when (style) {
      is CheckboxStyle.Label -> LabelTopPadding
      else -> TopPadding
    }
    val bottomPadding = when (style) {
      is CheckboxStyle.Label -> LabelBottomPadding
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
    override val OptionTextFont = TypographyKeyTokens.BodyMedium
    override val LabelTextFont = TypographyKeyTokens.Label
    override val HorizontalPadding = 16.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
  }

  data object MediumSizeCheckboxTokens : SizeCheckboxTokens() {
    override val TopPadding = 19.dp
    override val BottomPadding = 21.dp
    override val LabelTopPadding = 10.dp
    override val LabelBottomPadding = 12.dp
    override val OptionTextFont = TypographyKeyTokens.BodySmall
    override val LabelTextFont = TypographyKeyTokens.Label
    override val HorizontalPadding = 16.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
  }

  data object SmallSizeCheckboxTokens : SizeCheckboxTokens() {
    override val TopPadding = 15.dp
    override val BottomPadding = 17.dp
    override val LabelTopPadding = 7.dp
    override val LabelBottomPadding = 10.dp
    override val OptionTextFont = TypographyKeyTokens.BodySmall
    override val LabelTextFont = TypographyKeyTokens.FinePrint
    override val HorizontalPadding = 16.dp
    override val ContainerShape = ShapeKeyTokens.CornerLarge
  }
}
