package com.hedvig.android.core.uidata

import androidx.compose.runtime.Immutable
import giraffe.fragment.MonetaryAmountFragment
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import octopus.fragment.MoneyFragment
import octopus.type.CurrencyCode
import java.math.BigDecimal
import java.text.DecimalFormat

@Immutable
@Serializable
data class UiMoney(val amount: Double, val currencyCode: CurrencyCode) {
  @Transient
  val decimalFormatter: DecimalFormat = DecimalFormat("#")

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

    fun fromMonetaryAmountFragment(fragment: MonetaryAmountFragment?): UiMoney? {
      val amount = fragment?.amount?.toDoubleOrNull() ?: return null
      val currency = CurrencyCode.safeValueOf(fragment.currency)
      if (currency == CurrencyCode.UNKNOWN__) return null
      return UiMoney(amount, currency)
    }
  }
}
