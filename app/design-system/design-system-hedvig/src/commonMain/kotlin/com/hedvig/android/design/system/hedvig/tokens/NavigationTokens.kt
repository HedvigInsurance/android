package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.unit.dp

internal object NavigationTokens {
  val IconColor = ColorSchemeKeyTokens.FillPrimary
  val TextColor = ColorSchemeKeyTokens.TextPrimary
  val UnselectedIconColor = ColorSchemeKeyTokens.FillSecondaryTransparent
  val UnselectedTextColor = ColorSchemeKeyTokens.TextSecondaryTranslucent
  val TextStyle = TypographyKeyTokens.FinePrint
  val BorderColor = ColorSchemeKeyTokens.FillTertiary
  val IconSize = 24.dp
  val IndicatorWidth = IconSize * 2

  /**
   * The maximum font scale for the label text. This is to prevent the label from being too large and having to wrap or
   * truncate the text which is discouraged in the spec
   * https://m3.material.io/components/navigation-bar/guidelines#5a0e52ea-eb3f-42ef-96a4-160c7faf3c93
   */
  val NavigationBarFontScaleCap: Float = 1.15f
}

internal object NavigationBarTokens {
  val ItemTopPadding = 4.dp
  val ItemBottomPadding = 12.dp
  val ItemHorizontalPadding = 0.dp
}

internal object NavigationRailTokens {
  val ContainerTopPadding = 12.dp
  val ItemTopPadding = 6.dp
  val ItemBottomPadding = 6.dp
  val ItemHorizontalPadding = 4.dp
}
