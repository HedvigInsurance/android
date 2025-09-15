package com.hedvig.android.core.uidata

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import octopus.fragment.ItemCostFragment
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
      amount = amount + other.amount.toDouble(),
    )
  }

  companion object {
    fun fromMoneyFragment(fragment: MoneyFragment): UiMoney {
      return UiMoney(fragment.amount, fragment.currencyCode.toUiCurrencyCode())
    }
  }
}

@Serializable
data class ItemCost(
  val monthlyNet: UiMoney,
  val monthlyGross: UiMoney,
  val discounts: List<ItemCostDiscount>,
) {
  companion object {
    fun fromItemCostFragment(fragment: ItemCostFragment): ItemCost {
      return ItemCost(
        monthlyNet = UiMoney.fromMoneyFragment(fragment.monthlyNet),
        monthlyGross = UiMoney.fromMoneyFragment(fragment.monthlyGross),
        discounts = fragment.discounts.map {
          ItemCostDiscount(
            campaignCode = it.campaignCode,
            displayName = it.displayName,
            displayValue = it.displayValue,
            explanation = it.explanation,
          )
        },
      )
    }
  }
}

@Serializable
data class ItemCostDiscount(
  val campaignCode: String,
  val displayName: String,
  val displayValue: String,
  val explanation: String,
)
