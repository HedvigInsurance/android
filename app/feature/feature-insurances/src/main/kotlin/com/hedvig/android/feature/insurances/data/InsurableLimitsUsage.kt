package com.hedvig.android.feature.insurances.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate

/**
 * How much of a contract's insurable limits and deductible has been used in one contract year.
 */
internal data class InsurableLimitsUsage(
  val periodStartDate: LocalDate,
  val periodEndDate: LocalDate,
  val total: LimitUsage,
  /** Part of [total], tracked separately. Null if the product has no separate medicine limit. */
  val medicine: LimitUsage?,
  val deductible: LimitUsage,
) {
  data class LimitUsage(
    val used: UiMoney,
    val limit: UiMoney,
    val remaining: UiMoney,
  ) {
    val usedFraction: Float
      get() = if (limit.amount <= 0.0) 0f else (used.amount / limit.amount).toFloat().coerceIn(0f, 1f)
  }
}
