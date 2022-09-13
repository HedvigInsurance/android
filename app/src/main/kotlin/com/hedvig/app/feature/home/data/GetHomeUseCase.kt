package com.hedvig.app.feature.home.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.graphql.HomeQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.OperationResult
import com.hedvig.app.util.apollo.safeExecute
import com.hedvig.app.util.apollo.toEither

class GetHomeUseCase(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
) {

  suspend operator fun invoke(forceReload: Boolean): Either<OperationResult.Error, HomeQuery.Data> {
    val apolloCall = apolloClient.query(homeQuery())
    if (forceReload) {
      apolloCall.fetchPolicy(FetchPolicy.NetworkOnly)
    }
    return apolloCall
      .safeExecute()
      .toEither()
  }

  private fun homeQuery() = HomeQuery(localeManager.defaultLocale(), localeManager.defaultLocale().rawValue)

  data class Error(val message: String?)
}
