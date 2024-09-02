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
  val PlusIconSize = 24.dp
}

internal object PerilSmallTokens {
  val CircleSize = 20.dp
  val PaddingTop = 15.dp
  val PaddingBottom = 17.dp
  val PaddingHorizontal = 16.dp
  val ExpandedPaddingTop = 0.dp
  val ExpandedPaddingBottom = 15.dp
  val LabelLineSpacerWidth = 8.dp
  val ExpandedPaddingStart = CircleSize + LabelLineSpacerWidth
  val ExpandedPaddingEnd = 8.dp
  val LabelTextFont = TypographyKeyTokens.BodySmall
  val DescriptionTextFont = TypographyKeyTokens.Label
  val VerticalSpaceBetween = 17.dp
}

internal object PerilLargeTokens {
  val CircleSize = 24.dp
  val PaddingTop = 16.dp
  val PaddingBottom = 18.dp
  val PaddingHorizontal = 16.dp
  val ExpandedPaddingTop = 0.dp
  val ExpandedPaddingBottom = 14.dp
  val LabelLineSpacerWidth = 8.dp
  val ExpandedPaddingStart = CircleSize + LabelLineSpacerWidth
  val ExpandedPaddingEnd = 8.dp
  val LabelTextFont = TypographyKeyTokens.BodyMedium
  val DescriptionTextFont = TypographyKeyTokens.Label
  val VerticalSpaceBetween = 18.dp
}
