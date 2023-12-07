package com.hedvig.android.feature.payments2.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class MemberCharge(
  val grossAmount: UiMoney,
  val netAmount: UiMoney,
  val id: String,
  val status: MemberChargeStatus,
  val dueDate: LocalDate,
  val failedCharge: FailedCharge?,
  val chargeBreakdowns: List<ChargeBreakdown>,
) {
  @Serializable
  data class FailedCharge(
    val fromDate: LocalDate,
    val toDate: LocalDate,
  )

  enum class MemberChargeStatus {
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
