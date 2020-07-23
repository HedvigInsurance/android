package com.hedvig.app.feature.embark

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.ApolloClientWrapper

class EmbarkRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun embarkStoryAsync(name: String) = apolloClientWrapper
        .apolloClient
        .query(EmbarkStoryQuery(name))
        .toDeferred()
}
