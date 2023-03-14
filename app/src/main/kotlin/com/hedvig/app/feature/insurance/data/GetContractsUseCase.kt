package com.hedvig.app.feature.insurance.data

import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import giraffe.InsuranceQuery

class GetContractsUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun invoke(): Either<ErrorMessage, InsuranceQuery.Data> {
    return either {
      val insuranceQueryData = apolloClient
        .query(InsuranceQuery(languageService.getGraphQLLocale()))
        .safeExecute()
        .toEither(::ErrorMessage)
        .bind()
      insuranceQueryData
    }
  }
}
