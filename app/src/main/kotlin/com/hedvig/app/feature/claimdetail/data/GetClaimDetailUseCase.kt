package com.hedvig.app.feature.claimdetail.data

import arrow.core.Either
import arrow.core.firstOrNone
import arrow.core.flatMap
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.graphql.ClaimDetailsQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.apollo.toEither

class GetClaimDetailUseCase(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
) {
  sealed interface Error {
    object NetworkError : Error
    object NoClaimFound : Error
  }

  private val queryCall: ApolloCall<ClaimDetailsQuery.Data>
    get() = apolloClient
      .query(ClaimDetailsQuery(localeManager.defaultLocale()))
      .fetchPolicy(FetchPolicy.NetworkOnly)

  suspend operator fun invoke(claimId: String): Either<Error, ClaimDetailsQuery.ClaimDetail> {
    return queryCall
      .safeQuery()
      .toEither {
        Error.NetworkError
      }
      .flatMap { data ->
        data.claimDetails
          .firstOrNone { it.id == claimId }
          .toEither {
            Error.NoClaimFound
          }
      }
  }
}
