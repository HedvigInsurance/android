package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object TooltipTokens {
  val ContainerShape = ShapeKeyTokens.CornerSmall
  val ArrowHeightDp = 5.dp
  val ArrowWidthDp = 12.dp
  val ArrowSpaceFromEdgeWhenOffCentered = 16.dp
  val PaddingStart: Dp = 12.dp
  val PaddingEnd: Dp = 12.dp
  val PaddingTop: Dp = 6.5.dp
  val PaddingBottom: Dp = 7.5.dp
  val DefaultMinWidth = 36.dp // A sane default, so that the arrow can always look attached to the squircle
  val DefaultMaxWidth = 280.dp
  val TextFont = TypographyKeyTokens.Label
}
