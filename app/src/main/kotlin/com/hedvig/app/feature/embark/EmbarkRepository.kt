package com.hedvig.app.feature.embark

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.EmbarkStoryQuery
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.apollo.toEither

class EmbarkRepository(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
) {
  suspend fun embarkStory(name: String): Either<ErrorMessage, EmbarkStoryQuery.Data> {
    return apolloClient
      .query(EmbarkStoryQuery(name, localeManager.defaultLocale().rawValue))
      .safeQuery()
      .toEither(::ErrorMessage)
  }
}
