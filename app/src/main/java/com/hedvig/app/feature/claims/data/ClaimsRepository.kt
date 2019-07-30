package com.hedvig.app.feature.claims.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.graphql.TriggerCallMeChatMutation
import com.hedvig.android.owldroid.graphql.TriggerClaimChatMutation
import com.hedvig.android.owldroid.type.TriggerClaimChatInput
import com.hedvig.app.ApolloClientWrapper
import io.reactivex.Observable

class ClaimsRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    private lateinit var claimsQuery: CommonClaimQuery

    fun fetchCommonClaims(): Observable<CommonClaimQuery.Data?> {
        claimsQuery = CommonClaimQuery
            .builder()
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(claimsQuery))
            .map { it.data() }
    }

    fun triggerClaimsChat(claimTypeId: String?): Observable<Response<TriggerClaimChatMutation.Data>> {
        val inputBuilder = TriggerClaimChatInput.builder()
        claimTypeId?.let { inputBuilder.claimTypeId(it) }
        val triggerClaimsChatMutation = TriggerClaimChatMutation
            .builder()
            .input(inputBuilder.build())
            .build()

        return Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(triggerClaimsChatMutation))
    }

    fun triggerCallMeChat() = Rx2Apollo.from(
        apolloClientWrapper.apolloClient.mutate(TriggerCallMeChatMutation()))
}

