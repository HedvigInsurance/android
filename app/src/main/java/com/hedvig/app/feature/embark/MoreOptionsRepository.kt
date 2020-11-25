package com.hedvig.app.feature.embark

import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.MoreOptionsQuery
import com.hedvig.app.ApolloClientWrapper

class MoreOptionsRepository(
    val apolloClientWrapper: ApolloClientWrapper
) {
    suspend fun memberId() = apolloClientWrapper.apolloClient.query(MoreOptionsQuery())
        .await()
}
