package com.hedvig.android.core.ui

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import octopus.fragment.MoneyFragment
import octopus.type.CurrencyCode
import java.text.DecimalFormat

@Immutable
@Serializable
data class UiMoney(val amount: Double, val currencyCode: CurrencyCode) {
  @Transient
  val decimalFormatter: DecimalFormat = DecimalFormat("0.#")

  override fun toString(): String {
    return buildString {
      append(decimalFormatter.format(amount))
      append(" ")
      if (currencyCode == CurrencyCode.UNKNOWN__) {
        error("Unknown currency code")
      }
      append(currencyCode)
    }
  }

  companion object {
    fun fromMoneyFragment(fragment: MoneyFragment): UiMoney {
      return UiMoney(fragment.amount, fragment.currencyCode)
    }
  }
}
