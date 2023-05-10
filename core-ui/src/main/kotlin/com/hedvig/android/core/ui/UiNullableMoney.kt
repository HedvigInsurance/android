package com.hedvig.android.core.ui

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import octopus.fragment.MoneyFragment
import octopus.type.CurrencyCode

@Immutable
@Serializable
data class UiNullableMoney(val amount: Double?, val currencyCode: CurrencyCode) {
  override fun toString(): String {
    return "${amount ?: "-"} $currencyCode"
  }

  companion object {
    fun fromMoneyFragment(fragment: MoneyFragment?): UiNullableMoney? {
      fragment ?: return null
      return UiNullableMoney(fragment.amount, fragment.currencyCode)
    }
  }
}
