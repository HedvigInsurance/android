package com.hedvig.app.feature.loggedin.ui

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.LoggedInQuery
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService

class LoggedInRepository(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun loggedInData(): Either<OperationResult.Error, LoggedInQuery.Data> = apolloClient
    .query(LoggedInQuery(languageService.getGraphQLLocale()))
    .safeExecute()
    .toEither()
}
