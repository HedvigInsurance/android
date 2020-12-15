package com.hedvig.app.feature.onboarding

import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.MemberIdQuery
import com.hedvig.app.ApolloClientWrapper

class MemberIdRepository(
    val apolloClientWrapper: ApolloClientWrapper
) {
    suspend fun memberId() = apolloClientWrapper.apolloClient.query(MemberIdQuery())
        .await()
}
