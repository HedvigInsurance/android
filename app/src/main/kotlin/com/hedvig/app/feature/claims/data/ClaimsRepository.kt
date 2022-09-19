package com.hedvig.app.feature.claims.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.CommonClaimQuery
import com.hedvig.app.util.GraphQLLocaleService

class ClaimsRepository(
  private val apolloClient: ApolloClient,
  private val localeManager: GraphQLLocaleService,
) {

  suspend fun fetchCommonClaims() = apolloClient
    .query(CommonClaimQuery(localeManager.defaultLocale())).execute()
}
