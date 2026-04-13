package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit

internal object TypographyTokens {
  val BodyLarge = TextStyleToken(
    fontFamily = TypeScaleTokens.BodyLargeFont,
    fontWeight = TypeScaleTokens.BodyLargeWeight,
    fontSize = TypeScaleTokens.BodyLargeSize,
    lineHeight = TypeScaleTokens.BodyLargeLineHeight,
    letterSpacing = TypeScaleTokens.BodyLargeTracking,
  )
  val BodyMedium = TextStyleToken(
    fontFamily = TypeScaleTokens.BodyMediumFont,
    fontWeight = TypeScaleTokens.BodyMediumWeight,
    fontSize = TypeScaleTokens.BodyMediumSize,
    lineHeight = TypeScaleTokens.BodyMediumLineHeight,
    letterSpacing = TypeScaleTokens.BodyMediumTracking,
  )

  // The default typography of the app
  val BodySmall = TextStyleToken(
    fontFamily = TypeScaleTokens.BodySmallFont,
    fontWeight = TypeScaleTokens.BodySmallWeight,
    fontSize = TypeScaleTokens.BodySmallSize,
    lineHeight = TypeScaleTokens.BodySmallLineHeight,
    letterSpacing = TypeScaleTokens.BodySmallTracking,
  )
  val DisplayLarge = TextStyleToken(
    fontFamily = TypeScaleTokens.DisplayLargeFont,
    fontWeight = TypeScaleTokens.DisplayLargeWeight,
    fontSize = TypeScaleTokens.DisplayLargeSize,
    lineHeight = TypeScaleTokens.DisplayLargeLineHeight,
    letterSpacing = TypeScaleTokens.DisplayLargeTracking,
  )
  val DisplayMedium = TextStyleToken(
    fontFamily = TypeScaleTokens.DisplayMediumFont,
    fontWeight = TypeScaleTokens.DisplayMediumWeight,
    fontSize = TypeScaleTokens.DisplayMediumSize,
    lineHeight = TypeScaleTokens.DisplayMediumLineHeight,
    letterSpacing = TypeScaleTokens.DisplayMediumTracking,
  )
  val DisplaySmall = TextStyleToken(
    fontFamily = TypeScaleTokens.DisplaySmallFont,
    fontWeight = TypeScaleTokens.DisplaySmallWeight,
    fontSize = TypeScaleTokens.DisplaySmallSize,
    lineHeight = TypeScaleTokens.DisplaySmallLineHeight,
    letterSpacing = TypeScaleTokens.DisplaySmallTracking,
  )
  val HeadlineLarge = TextStyleToken(
    fontFamily = TypeScaleTokens.HeadlineLargeFont,
    fontWeight = TypeScaleTokens.HeadlineLargeWeight,
    fontSize = TypeScaleTokens.HeadlineLargeSize,
    lineHeight = TypeScaleTokens.HeadlineLargeLineHeight,
    letterSpacing = TypeScaleTokens.HeadlineLargeTracking,
  )
  val HeadlineMedium = TextStyleToken(
    fontFamily = TypeScaleTokens.HeadlineMediumFont,
    fontWeight = TypeScaleTokens.HeadlineMediumWeight,
    fontSize = TypeScaleTokens.HeadlineMediumSize,
    lineHeight = TypeScaleTokens.HeadlineMediumLineHeight,
    letterSpacing = TypeScaleTokens.HeadlineMediumTracking,
  )
  val HeadlineSmall = TextStyleToken(
    fontFamily = TypeScaleTokens.HeadlineSmallFont,
    fontWeight = TypeScaleTokens.HeadlineSmallWeight,
    fontSize = TypeScaleTokens.HeadlineSmallSize,
    lineHeight = TypeScaleTokens.HeadlineSmallLineHeight,
    letterSpacing = TypeScaleTokens.HeadlineSmallTracking,
  )
  val Label = TextStyleToken(
    fontFamily = TypeScaleTokens.LabelFont,
    fontWeight = TypeScaleTokens.LabelWeight,
    fontSize = TypeScaleTokens.LabelSize,
    lineHeight = TypeScaleTokens.LabelLineHeight,
    letterSpacing = TypeScaleTokens.LabelTracking,
  )
  val FinePrint = TextStyleToken(
    fontFamily = TypeScaleTokens.FinePrintFont,
    fontWeight = TypeScaleTokens.FinePrintWeight,
    fontSize = TypeScaleTokens.FinePrintSize,
    lineHeight = TypeScaleTokens.FinePrintLineHeight,
    letterSpacing = TypeScaleTokens.FinePrintTracking,
  )
}

internal interface TextStyleToken {
  val fontFamily: FontFamilyToken
  val fontWeight: FontWeight
  val fontSize: TextUnit
  val lineHeight: TextUnit
  val letterSpacing: TextUnit
}

private fun TextStyleToken(
  fontFamily: FontFamilyToken,
  fontWeight: FontWeight,
  fontSize: TextUnit,
  lineHeight: TextUnit,
  letterSpacing: TextUnit,
): TextStyleToken = object : TextStyleToken {
  override val fontFamily = fontFamily
  override val fontWeight = fontWeight
  override val fontSize = fontSize
  override val lineHeight = lineHeight
  override val letterSpacing = letterSpacing
}

@Composable
internal fun TextStyleToken.toTextStyle(): TextStyle {
  return DefaultTextStyle.copy(
//    fontFamily = fontFamily.toFontFamily(),
    fontWeight = fontWeight,
    fontSize = fontSize,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
  )
}

internal expect val DefaultPlatformTextStyle: PlatformTextStyle

internal val DefaultLineHeightStyle = LineHeightStyle(
  alignment = LineHeightStyle.Alignment.Center,
  trim = LineHeightStyle.Trim.None,
)

internal val DefaultTextStyle = TextStyle.Default.copy(
  platformStyle = DefaultPlatformTextStyle,
  lineHeightStyle = DefaultLineHeightStyle,
)
