package com.hedvig.android.shared.foreverui.ui.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepository.ReferralError
import octopus.FullReferralsQuery
import octopus.MemberReferralInformationCodeUpdateMutation

internal class ForeverRepositoryImpl(
  private val apolloClient: ApolloClient,
) : ForeverRepository {
  private val referralsQuery = FullReferralsQuery()

  override suspend fun getReferralsData(): Either<ErrorMessage, FullReferralsQuery.Data> = apolloClient
    .query(referralsQuery)
    .fetchPolicy(FetchPolicy.NetworkOnly)
    .safeExecute(::ErrorMessage)

  override suspend fun updateCode(newCode: String): Either<ReferralError, String> = either {
    val result = apolloClient
      .mutation(MemberReferralInformationCodeUpdateMutation(newCode))
      .safeExecute { ReferralError(it.toString()) }
      .bind()

    val error = result.memberReferralInformationCodeUpdate.userError
    val referralInformation = result.memberReferralInformationCodeUpdate.referralInformation

    if (referralInformation != null) {
      referralInformation.code
    } else if (error != null) {
      raise(ReferralError(error.message))
    } else {
      raise(ReferralError(null))
    }
  }
}
