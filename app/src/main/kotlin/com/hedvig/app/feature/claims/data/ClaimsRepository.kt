package com.hedvig.app.feature.claims.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.language.LanguageService
import giraffe.CommonClaimQuery

class ClaimsRepository(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {

  suspend fun fetchCommonClaims() = apolloClient
    .query(CommonClaimQuery(languageService.getGraphQLLocale())).execute()
}
