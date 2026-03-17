package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy.NetworkFirst
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.payments.data.PaymentDetails.PaymentsInfo
import com.hedvig.android.logger.logcat
import octopus.PaymentHistoryWithDetailsQuery
import octopus.type.MemberPaymentConnectionStatus

internal interface GetChargeDetailsUseCase {
  suspend fun invoke(id: String?): Either<ErrorMessage, PaymentDetails>
}

internal class GetChargeDetailsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetChargeDetailsUseCase {
  override suspend fun invoke(id: String?): Either<ErrorMessage, PaymentDetails> = either {
    val currentMember = apolloClient.query(PaymentHistoryWithDetailsQuery())
      .fetchPolicy(NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()
      .currentMember

    val pastCharges = currentMember.pastCharges.map {
      it.toMemberCharge(currentMember.referralInformation)
    }.reversed()
    val futureMemberCharge = currentMember.futureCharge?.toMemberCharge(currentMember.referralInformation)
    val ongoingChargeWithThisId = currentMember
      .ongoingCharges
      .firstOrNull { it.id == id }
      ?.toMemberCharge(currentMember.referralInformation)
    val futureMemberChargeWithThisId = futureMemberCharge.takeIf { it?.id == id }
    val pastMemberChargeWithThisId = pastCharges.firstOrNull { it.id == id }
    val charge = futureMemberChargeWithThisId ?: pastMemberChargeWithThisId
    ?: ongoingChargeWithThisId ?: raise(ErrorMessage())
    val paymentsInfo = run {
      if (futureMemberChargeWithThisId == null && ongoingChargeWithThisId == null ) {
        // Only show payment connection information if the charge is a future charge or ongoing charge.
        // Otherwise, the payment connection info we get is not reliably correct.
        return@run PaymentsInfo.NoPresentableInfo
      }
      val paymentInformation = currentMember.paymentInformation
      when (paymentInformation.status) {
        MemberPaymentConnectionStatus.ACTIVE -> {
          PaymentsInfo.Active(
            displayName = paymentInformation.chargeMethod?.displayName,
            displayValue = paymentInformation.chargeMethod?.descriptor,
            paymentMethod = charge.chargeMethod,
          )
        }

        MemberPaymentConnectionStatus.PENDING,
        MemberPaymentConnectionStatus.NEEDS_SETUP,
        MemberPaymentConnectionStatus.UNKNOWN__,
          -> {
          PaymentsInfo.NoPresentableInfo
        }
      }
    }
    PaymentDetails(
      memberCharge = charge,
      pastCharges = pastCharges,
      upComingCharge = futureMemberCharge,
      paymentsInfo = paymentsInfo,
    )
  }
}

internal data class PaymentDetails(
  val memberCharge: MemberCharge,
  val pastCharges: List<MemberCharge>?,
  val paymentsInfo: PaymentsInfo,
  val upComingCharge: MemberCharge?,
) {
  fun getNextCharge(selectedMemberCharge: MemberCharge): MemberCharge? {
    val index = (pastCharges?.indexOf(selectedMemberCharge) ?: 0) + 1
    return if (pastCharges != null && index > pastCharges.size - 1) {
      null
    } else {
      pastCharges?.get(index)
    }
  }

  sealed interface PaymentsInfo {
    data class Active(
      val displayName: String?,
      val displayValue: String?,
      val paymentMethod:  MemberPaymentChargeMethod?
    ) : PaymentsInfo

    data object NoPresentableInfo : PaymentsInfo
  }
}
