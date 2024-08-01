package com.hedvig.android.core.uidata

import androidx.compose.runtime.Immutable
import java.text.DecimalFormat
import kotlinx.serialization.Serializable
import octopus.fragment.MoneyFragment

@Immutable
@Serializable
data class UiNullableMoney(val amount: Double?, val currencyCode: UiCurrencyCode) {
  override fun toString(): String {
    return buildString {
      if (amount != null) {
        append(decimalFormatter.format(amount))
      } else {
        append("-")
      }
      append(" ")
      append(
        when (currencyCode) {
          UiCurrencyCode.SEK -> "kr"
          UiCurrencyCode.DKK -> currencyCode.toString()
          UiCurrencyCode.NOK -> currencyCode.toString()
        },
      )
    }
  }

  companion object {
    fun fromMoneyFragment(fragment: MoneyFragment?): UiNullableMoney? {
      fragment ?: return null
      return UiNullableMoney(fragment.amount, fragment.currencyCode.toUiCurrencyCode())
    }
  }
}

private val decimalFormatter: DecimalFormat = DecimalFormat("")
