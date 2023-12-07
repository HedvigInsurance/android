package com.hedvig.android.feature.payments2.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import kotlinx.collections.immutable.toPersistentList
import octopus.UpcomingPaymentQuery
import octopus.fragment.MemberChargeFragment
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
      memberCharge = result.currentMember.futureCharge?.toMemberCharge(),
      pastCharges = result.currentMember.pastCharges.map { it.toMemberCharge() }.reversed(),
      paymentConnection = PaymentConnection(
        connectionInfo = result.currentMember.paymentInformation.connection?.let {
          PaymentConnection.ConnectionInfo(
            displayName = it.displayName,
            displayValue = it.descriptor,
          )
        },
        status = when (result.currentMember.paymentInformation.status) {
          MemberPaymentConnectionStatus.ACTIVE -> PaymentConnection.PaymentConnectionStatus.ACTIVE
          MemberPaymentConnectionStatus.PENDING -> PaymentConnection.PaymentConnectionStatus.PENDING
          MemberPaymentConnectionStatus.NEEDS_SETUP -> PaymentConnection.PaymentConnectionStatus.NEEDS_SETUP
          MemberPaymentConnectionStatus.UNKNOWN__ -> PaymentConnection.PaymentConnectionStatus.UNKNOWN
        },
      ),
    )
  }
}

private fun MemberChargeFragment.toMemberCharge() = MemberCharge(
  id = id ?: "",
  grossAmount = UiMoney.fromMoneyFragment(gross),
  netAmount = UiMoney.fromMoneyFragment(net),
  status = when (status) {
    MemberChargeStatus.UPCOMING -> MemberCharge.MemberChargeStatus.UPCOMING
    MemberChargeStatus.SUCCESS -> MemberCharge.MemberChargeStatus.SUCCESS
    MemberChargeStatus.PENDING -> MemberCharge.MemberChargeStatus.PENDING
    MemberChargeStatus.FAILED -> MemberCharge.MemberChargeStatus.FAILED
    MemberChargeStatus.UNKNOWN__ -> MemberCharge.MemberChargeStatus.UNKNOWN
  },
  dueDate = date,
  failedCharge = toFailedCharge(),
  chargeBreakdowns = contractsChargeBreakdown.map {
    MemberCharge.ChargeBreakdown(
      contractDisplayName = it.contract.currentAgreement.productVariant.displayName,
      contractDetails = it.contract.exposureDisplayName,
      grossAmount = UiMoney.fromMoneyFragment(it.gross),
      periods = it.periods.map {
        MemberCharge.ChargeBreakdown.Period(
          amount = UiMoney.fromMoneyFragment(it.amount),
          fromDate = it.fromDate,
          toDate = it.toDate,
          isPreviouslyFailedCharge = it.isPreviouslyFailedCharge,
        )
      }.toPersistentList(),
    )
  }.toPersistentList(),
)

private fun MemberChargeFragment.toFailedCharge(): MemberCharge.FailedCharge? {
  val previousChargesPeriods = contractsChargeBreakdown
    .flatMap { it.periods }
    .filter { it.isPreviouslyFailedCharge }

  val from = previousChargesPeriods.minOfOrNull { it.fromDate }
  val to = previousChargesPeriods.maxOfOrNull { it.toDate }

  return if (from != null && to != null) {
    MemberCharge.FailedCharge(
      from,
      to,
    )
  } else {
    null
  }
}
