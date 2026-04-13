package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import hedvig.resources.Res
import org.jetbrains.compose.resources.Font

internal object TypefaceTokens {
//  val Serif = FontFamilyToken.Serif
  val Sans = FontFamilyToken.Sans
  val WeightRegular = FontWeight.Normal
}

internal enum class FontFamilyToken {
  Serif,
  Sans,
}

//@Composable
//internal fun FontFamilyToken.toFontFamily(): FontFamily {
//  return when (this) {
//    FontFamilyToken.Serif -> SerifBookSmall
//    FontFamilyToken.Sans -> SansStandard
//  }
//}
//
//private val SerifBookSmall: FontFamily
//  @Composable
//  get() = FontFamily(
//    Font(Res.font.hedvig_letters_small),
//  )
//
//private val SansStandard: FontFamily
//  @Composable
//  get() = FontFamily(
//    Font(Res.font.hedvig_letters_standard),
//  )
