package com.hedvig.app.feature.claims.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.graphql.TriggerClaimChatMutation
import com.hedvig.android.owldroid.type.TriggerClaimChatInput
import com.hedvig.app.util.LocaleManager

class ClaimsRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {

    suspend fun fetchCommonClaims() = apolloClient
        .query(CommonClaimQuery(localeManager.defaultLocale())).await()

    suspend fun triggerClaimsChat(claimTypeId: String?) = apolloClient
        .mutate(
            TriggerClaimChatMutation(
                TriggerClaimChatInput(Input.fromNullable(claimTypeId))
            )
        ).await()
}
