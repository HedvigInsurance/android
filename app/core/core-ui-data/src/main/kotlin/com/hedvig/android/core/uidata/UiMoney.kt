package com.hedvig.android.core.uidata

import androidx.compose.runtime.Immutable
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlinx.serialization.Serializable
import octopus.fragment.MoneyFragment
import octopus.type.CurrencyCode

@Immutable
@Serializable
data class UiMoney(val amount: Double, val currencyCode: CurrencyCode) {
  override fun toString(): String {
    return buildString {
      append(decimalFormatter.format(amount))
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

  operator fun plus(other: UiMoney): UiMoney {
    return this.copy(
      amount = BigDecimal(amount).add(BigDecimal(other.amount)).toDouble(),
    )
  }

  companion object {
    fun fromMoneyFragment(fragment: MoneyFragment): UiMoney {
      return UiMoney(fragment.amount, fragment.currencyCode)
    }
  }
}

private val decimalFormatter: DecimalFormat = DecimalFormat("")
