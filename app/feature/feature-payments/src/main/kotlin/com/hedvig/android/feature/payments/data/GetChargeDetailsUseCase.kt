package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import octopus.PaymentHistoryWithDetailsQuery
import octopus.type.MemberPaymentConnectionStatus

internal interface GetChargeDetailsUseCase {
  suspend fun invoke(id: String?): Either<ErrorMessage, PaymentDetails>
}

internal class GetChargeDetailsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val clock: Clock,
) : GetChargeDetailsUseCase {
  override suspend fun invoke(id: String?): Either<ErrorMessage, PaymentDetails> = either {
    val result = apolloClient.query(PaymentHistoryWithDetailsQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()

    val pastCharges = result.currentMember.pastCharges.map {
      it.toMemberCharge(
        result.currentMember.redeemedCampaigns,
        result.currentMember.referralInformation,
        clock,
      )
    }.reversed()
    val futureMemberCharge = result.currentMember.futureCharge?.toMemberCharge(
      result.currentMember.redeemedCampaigns,
      result.currentMember.referralInformation,
      clock,
    )
    val futureMemberChargeWithThisId = futureMemberCharge.takeIf { it?.id == id }

    val pastMemberCharge = pastCharges.firstOrNull { it.id == id }
    PaymentDetails(
      memberCharge = futureMemberChargeWithThisId ?: pastMemberCharge ?: raise(ErrorMessage()),
      pastCharges = pastCharges,
      upComingCharge = futureMemberCharge,
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

@Serializable
internal data class PaymentDetails(
  val memberCharge: MemberCharge,
  val pastCharges: List<MemberCharge>?,
  val paymentConnection: PaymentConnection?,
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
}
