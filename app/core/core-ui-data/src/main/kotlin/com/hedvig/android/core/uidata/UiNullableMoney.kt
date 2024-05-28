package com.hedvig.android.core.uidata

import androidx.compose.runtime.Immutable
import java.text.DecimalFormat
import kotlinx.serialization.Serializable
import octopus.fragment.MoneyFragment
import octopus.type.CurrencyCode

@Immutable
@Serializable
data class UiNullableMoney(val amount: Double?, val currencyCode: CurrencyCode) {
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
          CurrencyCode.SEK -> "kr"
          CurrencyCode.DKK -> currencyCode.toString()
          CurrencyCode.NOK -> currencyCode.toString()
          CurrencyCode.UNKNOWN__ -> error("Unknown currency code")
        },
      )
    }
  }

  companion object {
    fun fromMoneyFragment(fragment: MoneyFragment?): UiNullableMoney? {
      fragment ?: return null
      return UiNullableMoney(fragment.amount, fragment.currencyCode)
    }
  }
}

private val decimalFormatter: DecimalFormat = DecimalFormat("")
