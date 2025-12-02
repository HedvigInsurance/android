package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

internal object TypefaceTokens {
  val Serif = SerifBookSmall
  val Sans = SansStandard
  val WeightRegular = FontWeight.Normal
}

private val SerifBookSmall = FontFamily(
  Font(hedvig.resources.R.font.hedvig_letters_small),
)

private val SansStandard = FontFamily(
  Font(hedvig.resources.R.font.hedvig_letters_standard),
)

val FontFamily.Companion.HedvigSerif
  get() = SerifBookSmall
val FontFamily.Companion.HedvigSans
  get() = SansStandard
