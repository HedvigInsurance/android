package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.dp

internal object TopAppBarTokens {
  val ContainerColor = ColorSchemeKeyTokens.BackgroundPrimary
  val ContentColor = ColorSchemeKeyTokens.TextPrimary
  val ContainerHeight = 64.dp
  val ContentHorizontalPadding = 16.dp

  // val TextStyle = TypographyKeyTokens.HeadlineSmall
  // TODO: comment back when the DS migration is over and remove the other one!
  val TextStyle = TypographyKeyTokens.OldDesignTopAppBar
}
