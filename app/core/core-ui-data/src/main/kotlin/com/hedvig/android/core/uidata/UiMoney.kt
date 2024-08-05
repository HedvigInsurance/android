package com.hedvig.android.core.uidata

import androidx.compose.runtime.Immutable
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlinx.serialization.Serializable
import octopus.fragment.MoneyFragment

@Immutable
@Serializable
data class UiMoney(val amount: Double, val currencyCode: UiCurrencyCode) {
  override fun toString(): String {
    return buildString {
      append(decimalFormatter.format(amount))
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

  operator fun plus(other: UiMoney): UiMoney {
    return this.copy(
      amount = BigDecimal(amount).add(BigDecimal(other.amount)).toDouble(),
    )
  }

  companion object {
    fun fromMoneyFragment(fragment: MoneyFragment): UiMoney {
      return UiMoney(fragment.amount, fragment.currencyCode.toUiCurrencyCode())
    }
  }
}

private val decimalFormatter: DecimalFormat = DecimalFormat("")
