package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.toPayinAccount
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.datetime.LocalDate
import octopus.ManualChargeInfoQuery
import octopus.type.MemberPaymentProvider

internal interface GetManualChargeInfoUseCase {
  suspend fun invoke(): Either<ErrorMessage, ManualChargeInfo>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetManualChargeInfoUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetManualChargeInfoUseCase {
  override suspend fun invoke(): Either<ErrorMessage, ManualChargeInfo> = either {
    val currentMember = apolloClient.query(ManualChargeInfoQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute(::ErrorMessage)
      .bind()
      .currentMember

    val showManualCharge = currentMember.missedChargeIdToChargeManually

    val showCancellationWarning =
      currentMember.activeContracts
        .any { it.terminationDueToMissedPayments && it.terminationDate != null }

    if (showManualCharge == null) {
      logcat { "GetManualChargeInfoUseCaseImpl: missedChargeIdToChargeManually is null" }
      raise(ErrorMessage())
    }

    val latestFailedPastCharge = currentMember.pastCharges
      .firstOrNull { it.id == showManualCharge }

    if (latestFailedPastCharge == null) {
      logcat { "GetManualChargeInfoUseCaseImpl: latestFailedPastCharge is null" }
      raise(ErrorMessage())
    }

    val currentMethods = currentMember.paymentMethods.payinMethods.mapNotNull { it.toPayinAccount() }

    ManualChargeInfo(
      chargeId = latestFailedPastCharge.id,
      missedDueDate = latestFailedPastCharge.date,
      amountDue = UiMoney.fromMoneyFragment(latestFailedPastCharge.net),
      currentMethods = currentMethods,
      availablePayinMethods = currentMember.paymentMethods.availableMethods
        .filter { it.supportsPayin }
        .map { it.provider },
      primaryPayinMethod = currentMethods.firstOrNull { it.isDefault },
      showCancellationWarning = showCancellationWarning,
    )
  }
}

internal data class ManualChargeInfo(
  val chargeId: String?,
  val missedDueDate: LocalDate,
  val amountDue: UiMoney,
  val currentMethods: List<PayinAccount>,
  val availablePayinMethods: List<MemberPaymentProvider>,
  /** The method the member is charged on, absent when none is connected or it is one we cannot show. */
  val primaryPayinMethod: PayinAccount?,
  val showCancellationWarning: Boolean,
)
