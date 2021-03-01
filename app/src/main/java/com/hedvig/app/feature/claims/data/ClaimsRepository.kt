package com.hedvig.app.feature.claims.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.graphql.TriggerCallMeChatMutation
import com.hedvig.android.owldroid.graphql.TriggerClaimChatMutation
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.android.owldroid.type.TriggerClaimChatInput

class ClaimsRepository(
    private val apolloClient: ApolloClient,
    private val defaultLocale: Locale,
) {

    suspend fun fetchCommonClaims() = apolloClient
        .query(CommonClaimQuery(defaultLocale)).await()

    suspend fun triggerClaimsChat(claimTypeId: String?) = apolloClient
        .mutate(
            TriggerClaimChatMutation(
                TriggerClaimChatInput(Input.fromNullable(claimTypeId))
            )
        ).await()

    suspend fun triggerCallMeChat() =
        apolloClient.mutate(TriggerCallMeChatMutation()).await()
}
