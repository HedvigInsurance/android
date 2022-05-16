package com.hedvig.app.feature.embark

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class EmbarkRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    suspend fun embarkStory(name: String): Either<ErrorMessage, EmbarkStoryQuery.Data> = apolloClient
        .query(EmbarkStoryQuery(name, localeManager.defaultLocale().rawValue))
        .safeQuery()
        .toEither(::ErrorMessage)
}
