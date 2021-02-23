package com.hedvig.app.feature.claims.data

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.graphql.TriggerCallMeChatMutation
import com.hedvig.android.owldroid.graphql.TriggerClaimChatMutation
import com.hedvig.android.owldroid.type.TriggerClaimChatInput
import com.hedvig.app.util.apollo.defaultLocale

class ClaimsRepository(
    private val apolloClient: ApolloClient,
    private val context: Context,
) {

    suspend fun fetchCommonClaims() = apolloClient
        .query(
            CommonClaimQuery(
                defaultLocale(context)
            )
        ).await()

    suspend fun triggerClaimsChat(claimTypeId: String?) = apolloClient
        .mutate(
            TriggerClaimChatMutation(
                TriggerClaimChatInput(Input.fromNullable(claimTypeId))
            )
        ).await()

    suspend fun triggerCallMeChat() =
        apolloClient.mutate(TriggerCallMeChatMutation()).await()
}
