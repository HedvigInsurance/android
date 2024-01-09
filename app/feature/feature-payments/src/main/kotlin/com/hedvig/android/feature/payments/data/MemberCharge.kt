package com.hedvig.android.feature.payments.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class MemberCharge(
  val grossAmount: UiMoney,
  val netAmount: UiMoney,
  val id: String,
  val status: MemberChargeStatus,
  val dueDate: LocalDate,
  val failedCharge: FailedCharge?,
  val chargeBreakdowns: List<ChargeBreakdown>,
  val discounts: List<Discount>,
  private val carriedAdjustment: UiMoney?,
  private val settlementAdjustment: UiMoney?,
) {
  fun carriedAdjustmentIfAboveZero(): UiMoney? = if (carriedAdjustment != null && carriedAdjustment.amount > 0) {
    carriedAdjustment
  } else {
    null
  }

  fun settlementAdjustmentIfAboveZero(): UiMoney? =
    if (settlementAdjustment != null && settlementAdjustment.amount > 0) {
      settlementAdjustment
    } else {
      null
    }

  @Serializable
  data class FailedCharge(
    val fromDate: LocalDate,
    val toDate: LocalDate,
  )

  internal enum class MemberChargeStatus {
    UPCOMING,
    SUCCESS,
    PENDING,
    FAILED,
    UNKNOWN,
  }

  @Serializable
  data class ChargeBreakdown(
    val contractDisplayName: String,
    val contractDetails: String,
    val grossAmount: UiMoney,
    val periods: List<Period>,
  ) {
    @Serializable
    data class Period(
      val amount: UiMoney,
      val fromDate: LocalDate,
      val toDate: LocalDate,
      val isPreviouslyFailedCharge: Boolean,
    )
  }
}
