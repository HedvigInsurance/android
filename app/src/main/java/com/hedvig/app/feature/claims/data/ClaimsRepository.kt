package com.hedvig.app.feature.claims.data

import android.content.Context
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.graphql.TriggerCallMeChatMutation
import com.hedvig.android.owldroid.graphql.TriggerClaimChatMutation
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale
import io.reactivex.Observable
import type.TriggerClaimChatInput

class ClaimsRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    private lateinit var claimsQuery: CommonClaimQuery

    fun fetchCommonClaims(): Observable<CommonClaimQuery.Data?> {
        claimsQuery = CommonClaimQuery(locale = defaultLocale(context))

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(claimsQuery))
            .map { it.data() }
    }

    fun triggerClaimsChat(claimTypeId: String?): Observable<Response<TriggerClaimChatMutation.Data>> {
        val inputBuilder = TriggerClaimChatInput(claimTypeId = Input.fromNullable(claimTypeId))
        val triggerClaimsChatMutation = TriggerClaimChatMutation(input = inputBuilder)

        return Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(triggerClaimsChatMutation))
    }

    fun triggerCallMeChat() = Rx2Apollo.from(
        apolloClientWrapper.apolloClient.mutate(TriggerCallMeChatMutation())
    )
}

