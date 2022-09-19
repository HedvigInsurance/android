package com.hedvig.app.feature.welcome

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.WelcomeQuery
import com.hedvig.app.util.GraphQLLocaleService

class WelcomeRepository(
  private val apolloClient: ApolloClient,
  private val localeManager: GraphQLLocaleService,
) {
  suspend fun fetchWelcomeScreens() = apolloClient
    .query(WelcomeQuery(localeManager.defaultLocale()))
    .execute()
}
