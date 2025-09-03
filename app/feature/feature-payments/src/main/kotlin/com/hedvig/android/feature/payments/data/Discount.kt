package com.hedvig.android.feature.payments.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class DiscountedContract(
  val contractId: String,
  val contractDisplayName: String,
  val appliedDiscounts: List<Discount>, // todo: just an assumption, don't have api yet
)

@Serializable
internal data class Discount(
  val code: String,
  val description: String?,
  val expiredState: ExpiredState,
  val amount: UiMoney?,
  val isReferral: Boolean,
) {
  @Serializable
  sealed interface ExpiredState {
    @Serializable
    data object NotExpired : ExpiredState

    @Serializable
    data object Pending : ExpiredState

    @Serializable
    data class AlreadyExpired(val expirationDate: LocalDate) : ExpiredState

    @Serializable
    data class ExpiringInTheFuture(val expirationDate: LocalDate) : ExpiredState

    companion object
  }
}
