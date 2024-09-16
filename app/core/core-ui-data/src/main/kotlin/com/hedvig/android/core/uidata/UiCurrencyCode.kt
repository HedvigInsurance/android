package com.hedvig.android.core.uidata

import kotlinx.serialization.Serializable
import octopus.type.CurrencyCode

@Serializable
enum class UiCurrencyCode {
  SEK,
  DKK,
  NOK,
  ;

  companion object {
    fun fromCurrencyCode(currencyCode: CurrencyCode): UiCurrencyCode {
      return when (currencyCode) {
        CurrencyCode.SEK -> UiCurrencyCode.SEK
        CurrencyCode.DKK -> UiCurrencyCode.DKK
        CurrencyCode.NOK -> UiCurrencyCode.NOK
        CurrencyCode.UNKNOWN__ -> error("Unknown currency code")
      }
    }
  }
}

internal fun CurrencyCode.toUiCurrencyCode(): UiCurrencyCode {
  return UiCurrencyCode.fromCurrencyCode(this)
}
