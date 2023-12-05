package com.hedvig.android.feature.payments2.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments2.data.PaymentOverview.PaymentConnection.PaymentConnectionStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import octopus.UpcomingPaymentQuery
import octopus.type.MemberChargeStatus
import octopus.type.MemberPaymentConnectionStatus

internal interface GetUpcomingPaymentUseCase {
  suspend fun invoke(): Either<ErrorMessage, PaymentOverview>
}

internal data class GetUpcomingPaymentUseCaseImpl(
  val apolloClient: ApolloClient,
) : GetUpcomingPaymentUseCase {
  override suspend fun invoke(): Either<ErrorMessage, PaymentOverview> = either {
    val result = apolloClient.query(UpcomingPaymentQuery())
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    PaymentOverview(
      futureCharge = result.currentMember.futureCharge?.toFutureCharge(),
      chargeBreakdowns = persistentListOf(),
      paymentConnection = PaymentOverview.PaymentConnection(
        connectionInfo = result.currentMember.paymentInformation.connection?.let {
          PaymentOverview.PaymentConnection.ConnectionInfo(
            displayName = it.displayName,
            displayValue = it.descriptor,
          )
        },
        status = when (result.currentMember.paymentInformation.status) {
          MemberPaymentConnectionStatus.ACTIVE -> PaymentConnectionStatus.ACTIVE
          MemberPaymentConnectionStatus.PENDING -> PaymentConnectionStatus.PENDING
          MemberPaymentConnectionStatus.NEEDS_SETUP -> PaymentConnectionStatus.NEEDS_SETUP
          MemberPaymentConnectionStatus.UNKNOWN__ -> PaymentConnectionStatus.UNKNOWN
        },
      ),
    )
  }
}

private fun UpcomingPaymentQuery.Data.CurrentMember.FutureCharge.toFutureCharge() = PaymentOverview.FutureCharge(
  id = id ?: "",
  grossAmount = UiMoney.fromMoneyFragment(gross),
  netAmount = UiMoney.fromMoneyFragment(net),
  status = when (status) {
    MemberChargeStatus.UPCOMING -> PaymentOverview.MemberChargeStatus.UPCOMING
    MemberChargeStatus.SUCCESS -> PaymentOverview.MemberChargeStatus.SUCCESS
    MemberChargeStatus.PENDING -> PaymentOverview.MemberChargeStatus.PENDING
    MemberChargeStatus.FAILED -> PaymentOverview.MemberChargeStatus.FAILED
    MemberChargeStatus.UNKNOWN__ -> PaymentOverview.MemberChargeStatus.UNKNOWN
  },
  dueDate = date,
  failedCharge = toFailedCharge(),
)

private fun UpcomingPaymentQuery.Data.CurrentMember.FutureCharge.toFailedCharge(): PaymentOverview.FutureCharge.FailedCharge? {
  val previousChargesPeriods = contractsChargeBreakdown
    .flatMap { it.periods }
    .filter { it.isPreviouslyFailedCharge }

  val from = previousChargesPeriods.minOfOrNull { it.fromDate }
  val to = previousChargesPeriods.maxOfOrNull { it.toDate }

  return if (from != null && to != null) {
    PaymentOverview.FutureCharge.FailedCharge(
      from,
      to,
    )
  } else {
    null
  }
}

internal data class PaymentOverview(
  val futureCharge: FutureCharge?,
  val paymentConnection: PaymentConnection?,
  val chargeBreakdowns: ImmutableList<ChargeBreakdown>,
) {
  data class FutureCharge(
    val grossAmount: UiMoney,
    val netAmount: UiMoney,
    val id: String,
    val status: MemberChargeStatus,
    val dueDate: LocalDate,
    val failedCharge: FailedCharge?,
  ) {
    data class FailedCharge(
      val fromDate: LocalDate,
      val toDate: LocalDate,
    )
  }

  data class PaymentConnection(
    val connectionInfo: ConnectionInfo?,
    val status: PaymentConnectionStatus,
  ) {
    data class ConnectionInfo(
      val displayName: String,
      val displayValue: String,
    )

    enum class PaymentConnectionStatus {
      ACTIVE,
      PENDING,
      NEEDS_SETUP,
      UNKNOWN,
    }
  }

  data class ChargeBreakdown(
    val contractDisplayName: String,
    val grossAmount: UiMoney,
    val periods: ImmutableList<Period>,
  ) {
    data class Period(
      val amount: UiMoney,
      val fromDate: LocalDate,
      val toDate: LocalDate,
      val isPreviouslyFailedCharge: Boolean,
    )
  }

  enum class MemberChargeStatus {
    UPCOMING,
    SUCCESS,
    PENDING,
    FAILED,
    UNKNOWN,
  }
}
