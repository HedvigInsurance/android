package com.hedvig.onboarding.moreoptions

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.MemberIdQuery

class MemberIdRepository(
    private val apolloClient: ApolloClient,
) {
    suspend fun memberId() = apolloClient
        .query(MemberIdQuery())
        .await()
}
