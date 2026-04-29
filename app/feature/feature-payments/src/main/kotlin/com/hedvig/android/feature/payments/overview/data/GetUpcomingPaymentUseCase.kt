package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.ManualChargeToPrompt
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.MemberChargeShortInfo
import com.hedvig.android.feature.payments.data.MemberPaymentChargeMethod
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentConnection.Active
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.PaymentOverview.OngoingCharge
import com.hedvig.android.feature.payments.data.toChargeMethod
import com.hedvig.android.feature.payments.data.toFailedCharge
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.logger.logcat
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.UpcomingPaymentQuery
import octopus.fragment.MemberChargeFragment
import octopus.type.MemberChargeStatus
import octopus.type.MemberPaymentConnectionStatus

internal interface GetUpcomingPaymentUseCase {
  suspend fun invoke(): Flow<Either<ErrorMessage, PaymentOverview>>
}

internal data class GetUpcomingPaymentUseCaseImpl(
  val apolloClient: ApolloClient,
  val featureManager: FeatureManager,
  val clock: Clock,
) : GetUpcomingPaymentUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, PaymentOverview>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        emitAll(
          apolloClient.query(UpcomingPaymentQuery())
            .fetchPolicy(FetchPolicy.NetworkFirst)
            .safeFlow {
              logcat { "GetUpcomingPaymentUseCaseImpl error: $it" }
              ErrorMessage()
            }
            .map { response ->
              either {
                val result = response.bind()
                val paymentConnection = run {
                  val paymentInformation = result.currentMember.paymentInformation
                  when (paymentInformation.status) {
                    MemberPaymentConnectionStatus.ACTIVE -> {
                      PaymentConnection.Active(
                        displayName = paymentInformation.chargeMethod?.displayName,
                        displayValue = paymentInformation.chargeMethod?.descriptor,
                        chargeMethod = paymentInformation.chargeMethod?.paymentMethod.toChargeMethod(),
                      )
                    }

                    MemberPaymentConnectionStatus.PENDING -> {
                      PaymentConnection.Pending
                    }

                    MemberPaymentConnectionStatus.NEEDS_SETUP -> {
                      val firstKnownTerminationDateForContractTerminatedDueToMissedPayments = result
                        .currentMember
                        .activeContracts
                        .filter { it.terminationDueToMissedPayments }
                        .mapNotNull { it.terminationDate }
                        .sorted()
                        .firstOrNull()
                      PaymentConnection.NeedsSetup(firstKnownTerminationDateForContractTerminatedDueToMissedPayments)
                    }

                    MemberPaymentConnectionStatus.UNKNOWN__ -> {
                      PaymentConnection.Unknown
                    }
                  }
                }
                val memberChargeShortInfo = result.currentMember.futureCharge?.toMemberChargeShortInfo()

                val missedChargeIdToChargeManually: String? = result.currentMember.missedChargeIdToChargeManually

                val isManualChargeAllowed = if (missedChargeIdToChargeManually!=null) {
                  val failedChargeNet = result.currentMember.pastCharges.firstOrNull {
                    it.id == missedChargeIdToChargeManually}?.net?.let { net ->
                      UiMoney.fromMoneyFragment(net)
                  }
                  if (failedChargeNet!=null) {
                    ManualChargeToPrompt(failedChargeNet)
                  } else null
                } else {
                  null
                }

                PaymentOverview(
                  memberChargeShortInfo = memberChargeShortInfo,
                  ongoingCharges = result.currentMember.ongoingCharges.mapNotNull {
                    val id = it.id ?: return@mapNotNull null
                    OngoingCharge(id, it.date, UiMoney.fromMoneyFragment(it.net))
                  },
                  isManualChargeAllowed = isManualChargeAllowed,
                  paymentConnection = paymentConnection,
                )
              }
            },
        )
        delay(3.seconds)
      }
    }
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
  override suspend fun invoke(): Flow<Either<ErrorMessage, PaymentOverview>> {
    return flowOf(
      PaymentOverview(
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
      ).right(),
    )
  }
}
