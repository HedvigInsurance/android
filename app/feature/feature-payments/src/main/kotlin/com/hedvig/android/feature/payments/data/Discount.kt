package com.hedvig.android.feature.payments.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class Discount(
  val code: String,
  val description: String?,
  val expiredState: ExpiredState,
  val amount: UiMoney?,
  val isReferral: Boolean,
  val affectedContract: DiscountedContract? = null, // todo: just an assumption, don't have api yet
) {
  @Serializable
  sealed interface ExpiredState {
    @Serializable
    data object NotExpired : ExpiredState

    @Serializable
    data class AlreadyExpired(val expirationDate: LocalDate) : ExpiredState

    @Serializable
    data class ExpiringInTheFuture(val expirationDate: LocalDate) : ExpiredState

    companion object
  }
}

@Serializable
internal data class DiscountedContract(
  val contractId: String,
  val contractDisplayName: String,
)
