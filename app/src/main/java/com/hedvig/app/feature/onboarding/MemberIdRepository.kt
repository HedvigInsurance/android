package com.hedvig.app.feature.onboarding

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.MemberIdQuery
import javax.inject.Inject

class MemberIdRepository @Inject constructor(
    private val apolloClient: ApolloClient,
) {
    suspend fun memberId() = apolloClient
        .query(MemberIdQuery())
        .await()
}
