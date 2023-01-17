package com.hedvig.android.odyssey.model

import com.hedvig.common.remote.money.MonetaryAmount

sealed interface Resolution {
  object None : Resolution
  object ManualHandling : Resolution
  data class SingleItemPayout(
    val purchasePrice: MonetaryAmount,
    val depreciation: MonetaryAmount,
    val deductible: MonetaryAmount,
    val payoutAmount: MonetaryAmount,
  ) : Resolution
}
