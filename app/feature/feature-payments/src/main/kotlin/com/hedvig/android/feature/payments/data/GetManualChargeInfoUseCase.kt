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
): GetManualChargeInfoUseCase {
  override suspend fun invoke(): Either<ErrorMessage, ManualChargeInfo> = either {

    val currentMember = apolloClient.query(ManualChargeInfoQuery())
      .fetchPolicy(NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()
      .currentMember

    val showManualCharge = currentMember.missedChargeIdToChargeManually

    if (showManualCharge==null) {
      logcat {"GetManualChargeInfoUseCaseImpl: missedChargeIdToChargeManually is null"}
      raise(ErrorMessage())
    }

    val latestFailedPastCharge = currentMember.pastCharges
      .firstOrNull {it.id == showManualCharge}

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
}

internal data class ManualChargeInfo(
  val chargeId: String?,
  val missedDueDate: LocalDate,
  val amountDue: UiMoney,
  val bankDescriptor: String?,
  val bankAccountDisplayValue: String?
)
