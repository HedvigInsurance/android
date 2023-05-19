package com.hedvig.app.feature.home.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import giraffe.HomeQuery

class GetHomeUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {

  suspend operator fun invoke(forceReload: Boolean): Either<ErrorMessage, HomeQuery.Data> {
    val apolloCall = apolloClient.query(homeQuery())
    if (forceReload) {
      apolloCall.fetchPolicy(FetchPolicy.NetworkOnly)
    }
    return apolloCall
      .safeExecute()
      .toEither(::ErrorMessage)
  }

  private fun homeQuery() = HomeQuery(languageService.getGraphQLLocale(), languageService.getGraphQLLocale().rawValue)

  data class Error(val message: String?)
}
