package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

internal object DialogTokens {
  val ContainerColor = ColorSchemeKeyTokens.BackgroundPrimary
  val ContainerShape = ShapeKeyTokens.CornerXLarge
  val ShadowElevation = 2.dp
  val NoButtonsPadding = PaddingValues(16.dp)
  val TitlePlusButtonPadding = PaddingValues(top = 20.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
  val BigButtonsPadding = PaddingValues(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
  val SmallButtonsPadding = PaddingValues(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
  val ContentToButtonsPaddingHeight = 40.dp
  val ContentToTitlePlusButtonPaddingHeight = 24.dp
}
