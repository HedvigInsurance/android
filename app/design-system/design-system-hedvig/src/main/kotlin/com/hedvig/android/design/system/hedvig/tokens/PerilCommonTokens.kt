package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.dp

internal object PerilCommonTokens {
  val IconAnimationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow)
  val ContainerShape = ShapeKeyTokens.CornerLarge
  val ContainerColor = ColorSchemeKeyTokens.SurfacePrimary
  val TextColor = ColorSchemeKeyTokens.TextPrimary
  val NumbersColor = ColorSchemeKeyTokens.TextTertiary
  val DefaultIconColor = ColorSchemeKeyTokens.FillPrimary
  val DisabledTextColor = ColorSchemeKeyTokens.TextDisabled
  val DisabledIconColor = ColorSchemeKeyTokens.FillDisabled
}

internal object PerilSmallTokens {
  val PaddingTop = 15.dp
  val PaddingBottom = 17.dp
  val PaddingHorizontal = 16.dp
  val ExpandedPaddingTop = 0.dp
  val ExpandedPaddingBottom = 15.dp
  val ExpandedPaddingStart = 28.dp
  val ExpandedPaddingEnd = 8.dp
  val LabelTextFont = TypographyKeyTokens.BodySmall
  val DescriptionTextFont = TypographyKeyTokens.Label
  val VerticalSpaceBetween = 17.dp
}

internal object PerilLargeTokens {
  val PaddingTop = 16.dp
  val PaddingBottom = 18.dp
  val PaddingHorizontal = 16.dp
  val ExpandedPaddingTop = 0.dp
  val ExpandedPaddingBottom = 14.dp
  val ExpandedPaddingStart = 32.dp
  val ExpandedPaddingEnd = 8.dp
  val LabelTextFont = TypographyKeyTokens.BodyMedium
  val DescriptionTextFont = TypographyKeyTokens.Label
  val VerticalSpaceBetween = 18.dp
}
