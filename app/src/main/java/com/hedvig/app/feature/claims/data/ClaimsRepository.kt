package com.hedvig.app.feature.claims.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.app.util.LocaleManager

class ClaimsRepository(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
) {

  suspend fun fetchCommonClaims() = apolloClient
    .query(CommonClaimQuery(localeManager.defaultLocale())).execute()
}
