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
  suspend fun invoke(): Either<ErrorMessage, ManualChargeInfoResult>
}

/**
 * Separates "we could not load it" (the [Either] left) from the backend's own answer that there is
 * nothing here to charge, so a caller can keep a usable screen through a network blip without also
 * keeping one whose charge has gone away.
 */
internal sealed interface ManualChargeInfoResult {
  data class Chargeable(val info: ManualChargeInfo) : ManualChargeInfoResult

  /**
   * `missedChargeIdToChargeManually` came back null, which per the schema means the latest charge
   * either succeeded or the member may no longer settle it themselves.
   */
  data object NoLongerChargeable : ManualChargeInfoResult
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetManualChargeInfoUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetManualChargeInfoUseCase {
  override suspend fun invoke(): Either<ErrorMessage, ManualChargeInfoResult> = either {
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
      return@either ManualChargeInfoResult.NoLongerChargeable
    }

    val latestFailedPastCharge = currentMember.pastCharges
      .firstOrNull { it.id == showManualCharge }

    // The backend named a charge to settle but did not return it, which is not a state the member
    // can act on either way, so it stays a plain failure they can retry out of.
    if (latestFailedPastCharge == null) {
      logcat { "GetManualChargeInfoUseCaseImpl: latestFailedPastCharge is null" }
      raise(ErrorMessage())
    }

    val currentMethods = currentMember.paymentMethods.payinMethods.mapNotNull { it.toPayinAccount() }

    ManualChargeInfoResult.Chargeable(
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
      ),
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
