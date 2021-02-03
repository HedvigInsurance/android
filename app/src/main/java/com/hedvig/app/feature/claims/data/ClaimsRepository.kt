package com.hedvig.app.feature.claims.data

import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.graphql.TriggerCallMeChatMutation
import com.hedvig.android.owldroid.graphql.TriggerClaimChatMutation
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.android.owldroid.type.TriggerClaimChatInput
import com.hedvig.app.ApolloClientWrapper

class ClaimsRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val defaultLocale: Locale
) {

    suspend fun fetchCommonClaims() = apolloClientWrapper.apolloClient
        .query(CommonClaimQuery(defaultLocale))
        .await()

    suspend fun triggerClaimsChat(claimTypeId: String?) = apolloClientWrapper.apolloClient.mutate(
        TriggerClaimChatMutation(
            TriggerClaimChatInput(Input.fromNullable(claimTypeId))
        )
    ).await()

    suspend fun triggerCallMeChat() =
        apolloClientWrapper.apolloClient.mutate(TriggerCallMeChatMutation()).await()
}
