package com.hedvig.app.feature.keygear.data

import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.ApolloClientWrapper

class KeyGearItemsRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun keyGearItems() = Rx2Apollo.from(
        apolloClientWrapper
            .apolloClient
            .query(KeyGearItemsQuery())
            .watcher()
    )
}
