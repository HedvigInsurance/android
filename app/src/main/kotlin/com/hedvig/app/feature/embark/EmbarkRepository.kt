package com.hedvig.app.feature.embark

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.EmbarkStoryQuery
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService

class EmbarkRepository(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun embarkStory(name: String): Either<ErrorMessage, EmbarkStoryQuery.Data> {
    return apolloClient
      .query(EmbarkStoryQuery(name, languageService.getGraphQLLocale().rawValue))
      .safeExecute()
      .toEither(::ErrorMessage)
  }
}
