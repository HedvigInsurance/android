package com.hedvig.app.feature.embark

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toDeferred

class EmbarkRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun embarkStoryAsync(name: String) = apolloClientWrapper
        .apolloClient
        .query(EmbarkStoryQuery(name))
        .toDeferred()
}
