package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle

internal object TypographyTokens {
  val BodyLarge = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.BodyLargeFont,
    fontWeight = TypeScaleTokens.BodyLargeWeight,
    fontSize = TypeScaleTokens.BodyLargeSize,
    lineHeight = TypeScaleTokens.BodyLargeLineHeight,
    letterSpacing = TypeScaleTokens.BodyLargeTracking,
  )
  val BodyMedium = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.BodyMediumFont,
    fontWeight = TypeScaleTokens.BodyMediumWeight,
    fontSize = TypeScaleTokens.BodyMediumSize,
    lineHeight = TypeScaleTokens.BodyMediumLineHeight,
    letterSpacing = TypeScaleTokens.BodyMediumTracking,
  )

  // The default typography of the app
  val BodySmall = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.BodySmallFont,
    fontWeight = TypeScaleTokens.BodySmallWeight,
    fontSize = TypeScaleTokens.BodySmallSize,
    lineHeight = TypeScaleTokens.BodySmallLineHeight,
    letterSpacing = TypeScaleTokens.BodySmallTracking,
  )
  val DisplayLarge = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.DisplayLargeFont,
    fontWeight = TypeScaleTokens.DisplayLargeWeight,
    fontSize = TypeScaleTokens.DisplayLargeSize,
    lineHeight = TypeScaleTokens.DisplayLargeLineHeight,
    letterSpacing = TypeScaleTokens.DisplayLargeTracking,
  )
  val DisplayMedium = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.DisplayMediumFont,
    fontWeight = TypeScaleTokens.DisplayMediumWeight,
    fontSize = TypeScaleTokens.DisplayMediumSize,
    lineHeight = TypeScaleTokens.DisplayMediumLineHeight,
    letterSpacing = TypeScaleTokens.DisplayMediumTracking,
  )
  val DisplaySmall = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.DisplaySmallFont,
    fontWeight = TypeScaleTokens.DisplaySmallWeight,
    fontSize = TypeScaleTokens.DisplaySmallSize,
    lineHeight = TypeScaleTokens.DisplaySmallLineHeight,
    letterSpacing = TypeScaleTokens.DisplaySmallTracking,
  )
  val HeadlineLarge = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.HeadlineLargeFont,
    fontWeight = TypeScaleTokens.HeadlineLargeWeight,
    fontSize = TypeScaleTokens.HeadlineLargeSize,
    lineHeight = TypeScaleTokens.HeadlineLargeLineHeight,
    letterSpacing = TypeScaleTokens.HeadlineLargeTracking,
  )
  val HeadlineMedium = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.HeadlineMediumFont,
    fontWeight = TypeScaleTokens.HeadlineMediumWeight,
    fontSize = TypeScaleTokens.HeadlineMediumSize,
    lineHeight = TypeScaleTokens.HeadlineMediumLineHeight,
    letterSpacing = TypeScaleTokens.HeadlineMediumTracking,
  )
  val HeadlineSmall = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.HeadlineSmallFont,
    fontWeight = TypeScaleTokens.HeadlineSmallWeight,
    fontSize = TypeScaleTokens.HeadlineSmallSize,
    lineHeight = TypeScaleTokens.HeadlineSmallLineHeight,
    letterSpacing = TypeScaleTokens.HeadlineSmallTracking,
  )
  val Label = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.LabelFont,
    fontWeight = TypeScaleTokens.LabelWeight,
    fontSize = TypeScaleTokens.LabelSize,
    lineHeight = TypeScaleTokens.LabelLineHeight,
    letterSpacing = TypeScaleTokens.LabelTracking,
  )
  val FinePrint = DefaultTextStyle.copy(
    fontFamily = TypeScaleTokens.FinePrintFont,
    fontWeight = TypeScaleTokens.FinePrintWeight,
    fontSize = TypeScaleTokens.FinePrintSize,
    lineHeight = TypeScaleTokens.FinePrintLineHeight,
    letterSpacing = TypeScaleTokens.FinePrintTracking,
  )
}

private val DefaultPlatformTextStyle = PlatformTextStyle()

internal val DefaultLineHeightStyle = LineHeightStyle(
  alignment = LineHeightStyle.Alignment.Center,
  trim = LineHeightStyle.Trim.None,
)

internal val DefaultTextStyle = TextStyle.Default.copy(
  platformStyle = DefaultPlatformTextStyle,
  lineHeightStyle = DefaultLineHeightStyle,
)
