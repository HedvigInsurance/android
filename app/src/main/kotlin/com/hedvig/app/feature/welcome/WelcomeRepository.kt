package com.hedvig.app.feature.welcome

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.language.LanguageService
import giraffe.WelcomeQuery

class WelcomeRepository(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun fetchWelcomeScreens() = apolloClient
    .query(WelcomeQuery(languageService.getGraphQLLocale()))
    .execute()
}
