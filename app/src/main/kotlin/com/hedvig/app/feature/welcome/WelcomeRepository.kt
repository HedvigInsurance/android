package com.hedvig.app.feature.welcome

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.WelcomeQuery
import com.hedvig.android.language.LanguageService

class WelcomeRepository(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun fetchWelcomeScreens() = apolloClient
    .query(WelcomeQuery(languageService.getGraphQLLocale()))
    .execute()
}
