package com.hedvig.app.feature.embark

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class EmbarkRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    suspend fun embarkStory(name: String): QueryResult<EmbarkStoryQuery.Data> = apolloClient
        .query(EmbarkStoryQuery(name, localeManager.defaultLocale().rawValue))
        .safeQuery()
}
