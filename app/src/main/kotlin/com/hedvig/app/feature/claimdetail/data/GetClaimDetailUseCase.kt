package com.hedvig.app.feature.claimdetail.data

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.firstOrNone
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.graphql.ClaimDetailsQuery
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService

class GetClaimDetailUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  sealed interface Error {
    object NetworkError : Error
    object NoClaimFound : Error
  }

  private val queryCall: ApolloCall<ClaimDetailsQuery.Data>
    get() = apolloClient
      .query(ClaimDetailsQuery(languageService.getGraphQLLocale()))
      .fetchPolicy(FetchPolicy.NetworkOnly)

  suspend operator fun invoke(claimId: String): Either<Error, ClaimDetailsQuery.ClaimDetail> {
    return either {
      val data = queryCall.safeExecute().toEither { Error.NetworkError }.bind()
      data.claimDetails.firstOrNone { it.id == claimId }.bind { Error.NoClaimFound }
    }
  }
}
