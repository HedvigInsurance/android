package com.hedvig.android.feature.payments.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class DiscountedContract(
  val contractId: String,
  val contractDisplayName: String,
  val discountsDetails: DiscountsDetails
)

@Serializable
internal data class DiscountsDetails (
  val discountInfo: String?,
  val appliedDiscounts: List<Discount>,
)

@Serializable
internal data class Discount(
  val code: String,
  val description: String?,
  val status: DiscountStatus,
  val statusDescription: String?,
  val amount: UiMoney?,
  val isReferral: Boolean,
) {
  enum class DiscountStatus {
    ACTIVE,
    PENDING,
    EXPIRED
  }
}

