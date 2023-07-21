package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import giraffe.InsuranceContractsQuery

internal class GetInsuranceContractsUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun invoke(): Either<ErrorMessage, List<InsuranceContractsQuery.Contract>> {
    return either {
      val insuranceQueryData = apolloClient
        .query(InsuranceContractsQuery(languageService.getGraphQLLocale()))
        .fetchPolicy(FetchPolicy.NetworkFirst)
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
      insuranceQueryData.contracts
    }
  }
}
