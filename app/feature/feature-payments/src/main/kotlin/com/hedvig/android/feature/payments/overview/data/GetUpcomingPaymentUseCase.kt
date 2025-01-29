package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.MemberChargeShortInfo
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.PaymentOverview.OngoingCharge
import com.hedvig.android.feature.payments.data.toFailedCharge
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.UpcomingPaymentQuery
import octopus.fragment.MemberChargeFragment
import octopus.type.MemberChargeStatus
import octopus.type.MemberPaymentConnectionStatus

internal interface GetUpcomingPaymentUseCase {
  suspend fun invoke(): Either<ErrorMessage, PaymentOverview>
}

internal data class GetUpcomingPaymentUseCaseImpl(
  val apolloClient: ApolloClient,
  val clock: Clock,
) : GetUpcomingPaymentUseCase {
  override suspend fun invoke(): Either<ErrorMessage, PaymentOverview> = either {
    val result = apolloClient.query(UpcomingPaymentQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()

    PaymentOverview(
      memberChargeShortInfo = result.currentMember.futureCharge?.toMemberChargeShortInfo(),
      ongoingCharges = result.currentMember.ongoingCharges.mapNotNull {
        val id = it.id ?: return@mapNotNull null
        OngoingCharge(id, it.date, UiMoney.fromMoneyFragment(it.net))
      },
      paymentConnection = run {
        val paymentInformation = result.currentMember.paymentInformation
        when (paymentInformation.status) {
          MemberPaymentConnectionStatus.ACTIVE -> {
            if (paymentInformation.connection == null) {
              logcat(LogPriority.ERROR) { "Payment connection is active but connection is null" }
              PaymentConnection.Unknown
            } else {
              PaymentConnection.Active(
                displayName = paymentInformation.connection.displayName,
                displayValue = paymentInformation.connection.descriptor,
              )
            }
          }

          MemberPaymentConnectionStatus.PENDING -> PaymentConnection.Pending
          MemberPaymentConnectionStatus.NEEDS_SETUP -> PaymentConnection.NeedsSetup
          MemberPaymentConnectionStatus.UNKNOWN__ -> PaymentConnection.Unknown
        }
      },
    )
  }
}

private fun MemberChargeFragment.toMemberChargeShortInfo() = MemberChargeShortInfo(
  id = id,
  netAmount = UiMoney.fromMoneyFragment(net),
  dueDate = date,
  failedCharge = toFailedCharge(),
  status = when (status) {
    MemberChargeStatus.UPCOMING -> MemberCharge.MemberChargeStatus.UPCOMING
    MemberChargeStatus.SUCCESS -> MemberCharge.MemberChargeStatus.SUCCESS
    MemberChargeStatus.PENDING -> MemberCharge.MemberChargeStatus.PENDING
    MemberChargeStatus.FAILED -> MemberCharge.MemberChargeStatus.FAILED
    MemberChargeStatus.UNKNOWN__ -> MemberCharge.MemberChargeStatus.UNKNOWN
  },
)

internal class GetUpcomingPaymentUseCaseDemo(
  private val clock: Clock,
) : GetUpcomingPaymentUseCase {
  override suspend fun invoke(): Either<ErrorMessage, PaymentOverview> {
    return PaymentOverview(
      MemberChargeShortInfo(
        netAmount = UiMoney(100.0, UiCurrencyCode.SEK),
        id = "id",
        status = MemberCharge.MemberChargeStatus.SUCCESS,
        dueDate = (clock.now() + 10.days).toLocalDateTime(TimeZone.UTC).date,
        failedCharge = null,
      ),
      emptyList(),
      null,
    ).right()
  }
}
