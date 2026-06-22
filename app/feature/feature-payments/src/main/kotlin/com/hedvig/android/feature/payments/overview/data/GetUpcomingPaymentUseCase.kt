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
import com.hedvig.android.data.paying.member.GetMemberTypeUseCase
import com.hedvig.android.data.paying.member.MemberType
import com.hedvig.android.feature.payments.data.ManualChargeToPrompt
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.MemberChargeShortInfo
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentConnection.*
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.PaymentOverview.OngoingCharge
import com.hedvig.android.feature.payments.data.toFailedCharge
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.UpcomingPaymentQuery
import octopus.fragment.MemberChargeFragment
import octopus.type.MemberChargeStatus
import octopus.type.MemberPaymentMethodStatus

internal interface GetUpcomingPaymentUseCase {
  suspend fun invoke(): Either<ErrorMessage, PaymentOverview>
}

@Inject
internal data class GetUpcomingPaymentUseCaseImpl(
  val apolloClient: ApolloClient,
  val clock: Clock,
  val getMemberTypeUseCase: GetMemberTypeUseCase,
) : GetUpcomingPaymentUseCase {
  override suspend fun invoke(): Either<ErrorMessage, PaymentOverview> = either {
    val result = apolloClient.query(UpcomingPaymentQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()
    val memberType = getMemberTypeUseCase.invoke().bind()

    val missedChargeIdToChargeManually: String? =
      result.currentMember.missedChargeIdToChargeManually

    val isManualChargeAllowed = if (missedChargeIdToChargeManually != null) {
      val failedChargeNet = result.currentMember.pastCharges.firstOrNull {
        it.id == missedChargeIdToChargeManually
      }?.net?.let { net ->
        UiMoney.fromMoneyFragment(net)
      }
      if (failedChargeNet != null) {
        ManualChargeToPrompt(failedChargeNet)
      } else {
        null
      }
    } else {
      null
    }

    PaymentOverview(
      memberChargeShortInfo = result.currentMember.futureCharge?.toMemberChargeShortInfo(),
      ongoingCharges = result.currentMember.ongoingCharges.mapNotNull {
        val id = it.id ?: return@mapNotNull null
        OngoingCharge(id, it.date, UiMoney.fromMoneyFragment(it.net))
      },
      paymentConnection = run {
        val paymentMethods = result.currentMember.paymentMethods
        val payinMethod = paymentMethods.defaultPayinMethod
          ?: paymentMethods.payinMethods.find { it.isDefault }
        logcat {"Mariia: payinMethod $payinMethod"}
        val payoutMethod = paymentMethods.defaultPayoutMethod
          ?: paymentMethods.payoutMethods.find { it.isDefault }
        if (payinMethod == null) {
          val firstKnownTerminationDateForContractTerminatedDueToMissedPayments = result
            .currentMember
            .activeContracts
            .filter { it.terminationDueToMissedPayments }
            .mapNotNull { it.terminationDate }
            .sorted()
            .firstOrNull()

          when (memberType) {

            MemberType.STANDARD_MEMBER -> return@run NeedsPayinSetup(
              firstKnownTerminationDateForContractTerminatedDueToMissedPayments,
            )

            MemberType.QASA_ONLY_MEMBER -> {
              if (payoutMethod == null) {
                return@run PaymentConnection.NeedsPayoutSetup
              } else return@run PaymentConnection.Active
            }

            MemberType.STANDARD_TO_QASA_MEMBER -> TODO()
          }
        }
        when (payinMethod.status) {
          MemberPaymentMethodStatus.ACTIVE -> {
            logcat {"Mariia: MemberPaymentMethodStatus.ACTIVE"}
            logcat {"Mariia: payoutMethod $payoutMethod"}
            if (payoutMethod == null) {
              return@run PaymentConnection.NeedsPayoutSetup
            } else return@run PaymentConnection.Active
          }

          MemberPaymentMethodStatus.PENDING -> PaymentConnection.Pending
          MemberPaymentMethodStatus.UNKNOWN__ -> PaymentConnection.Unknown
        }
      },
      isManualChargeAllowed = isManualChargeAllowed,
      memberType = memberType
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

@Inject
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
      PaymentConnection.Unknown,
      isManualChargeAllowed = null,
      memberType = MemberType.STANDARD_MEMBER
    ).right()
  }
}
