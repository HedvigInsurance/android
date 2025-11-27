package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.context.bind
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.MemberPaymentDetailsQuery

internal interface GetMemberPaymentsDetailsUseCase {
  suspend fun invoke(): Either<ErrorMessage, MemberPaymentsDetails>
}

internal class GetMemberPaymentsDetailsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetMemberPaymentsDetailsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, MemberPaymentsDetails> {
    return either {
      val result = apolloClient.query(MemberPaymentDetailsQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute(::ErrorMessage)
        .onLeft {
          logcat(LogPriority.WARN) { "GetMemberPaymentsDetailsUseCase returned error: $it" }
        }
        .bind()
      val chargeMethod = result.currentMember.paymentInformation.chargeMethod
      if (chargeMethod == null) {
        logcat(LogPriority.WARN) { "GetMemberPaymentsDetailsUseCase chargeMethod is null" }
        raise(ErrorMessage())
      }
      with(chargeMethod) {
        MemberPaymentsDetails(
          chargingDayInTheMonth = chargingDayInTheMonth,
          descriptor = descriptor,
          displayName = displayName,
          mandate = mandate,
          paymentMethod = paymentMethod,
        )
      }
    }
  }
}

data class MemberPaymentsDetails(
  val chargingDayInTheMonth: Int?,
  val descriptor: String?,
  val displayName: String?,
  val mandate: String?,
  val paymentMethod: String,
)
