package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.datetime.Clock
import octopus.PaymentHistoryQuery

internal interface GetChargeDetailsUseCase {
  suspend fun invoke(id: String): Either<ErrorMessage, MemberCharge>
}

internal class GetChargeDetailsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val clock: Clock,
) : GetChargeDetailsUseCase {
  override suspend fun invoke(id: String): Either<ErrorMessage, MemberCharge> = either {
    val result = apolloClient.query(PaymentHistoryQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    val pastCharges = result.currentMember.pastCharges.map {
      it.toMemberCharge(
        result.currentMember.redeemedCampaigns,
        result.currentMember.referralInformation,
        clock,
      )
    }
    pastCharges.firstOrNull { it.id == id } ?: raise(ErrorMessage())
  }
}
