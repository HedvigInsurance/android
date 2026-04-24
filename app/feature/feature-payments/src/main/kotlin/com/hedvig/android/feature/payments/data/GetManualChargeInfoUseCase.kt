package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy.NetworkFirst
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import octopus.ManualChargeInfoQuery
import octopus.type.MemberChargeStatus
import com.hedvig.android.logger.logcat
import octopus.type.MemberPaymentConnectionStatus

internal interface GetManualChargeInfoUseCase {
  suspend fun invoke(): Either<ErrorMessage, ManualChargeInfo>
}

internal class GetManualChargeInfoUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
): GetManualChargeInfoUseCase {
  override suspend fun invoke(): Either<ErrorMessage, ManualChargeInfo> = either {

    val isFeatureEnabled = featureManager.isFeatureEnabled(Feature.ENABLE_MANUAL_CHARGE).first()
    if (!isFeatureEnabled) {
      logcat {"GetManualChargeInfoUseCaseImpl: manual charge FF is off"}
      raise(ErrorMessage())
    }

    val currentMember = apolloClient.query(ManualChargeInfoQuery())
      .fetchPolicy(NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()
      .currentMember

    val isPaymentMethodTrustly = currentMember.paymentInformation.status == MemberPaymentConnectionStatus.ACTIVE &&
      currentMember.paymentInformation.chargeMethod?.paymentMethod.toChargeMethod() ==
      MemberPaymentChargeMethod.TRUSTLY

    if (!isPaymentMethodTrustly) {
      logcat {"GetManualChargeInfoUseCaseImpl: payment method not Trustly"}
      raise(ErrorMessage())
    }

    val isFailedInUpcomingPayment = currentMember.futureCharge?.chargeBreakdown
    ?.flatMap { it.periods }
    ?.any { it.isPreviouslyFailedCharge } == true
    if (!isFailedInUpcomingPayment) {
      logcat {"GetManualChargeInfoUseCaseImpl: no failed in upcoming payment"}
      raise(ErrorMessage())
    }

    val latestFailedPastCharge = currentMember.pastCharges
      .maxByOrNull { it.date }
      .takeIf { it?.status == MemberChargeStatus.FAILED }

    if (latestFailedPastCharge==null) {
      logcat {"GetManualChargeInfoUseCaseImpl: latestFailedPastCharge is null"}
      raise(ErrorMessage())
    }

    ManualChargeInfo(
      chargeId = latestFailedPastCharge.id,
      missedDueDate = latestFailedPastCharge.date,
      amountDue = UiMoney.fromMoneyFragment(latestFailedPastCharge.net),
      bankAccountDisplayValue = currentMember.paymentInformation.chargeMethod?.displayName,
      bankDescriptor = currentMember.paymentInformation.chargeMethod?.descriptor
    )
  }
//TODO: all these flags will be moved to BE
}

internal data class ManualChargeInfo(
  val chargeId: String?,
  val missedDueDate: LocalDate,
  val amountDue: UiMoney,
  val bankDescriptor: String?,
  val bankAccountDisplayValue: String?
)
