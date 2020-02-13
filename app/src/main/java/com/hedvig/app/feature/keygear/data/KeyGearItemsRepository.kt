package com.hedvig.app.feature.keygear.data

import com.apollographql.apollo.coroutines.toChannel
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.ApolloClientWrapper

class KeyGearItemsRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun keyGearItems() =
        apolloClientWrapper
            .apolloClient
            .query(KeyGearItemsQuery())
            .watcher()
            .toChannel()
}
