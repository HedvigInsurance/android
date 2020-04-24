package com.hedvig.app.feature.claims.data

import android.content.Context
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.graphql.TriggerCallMeChatMutation
import com.hedvig.android.owldroid.graphql.TriggerClaimChatMutation
import com.hedvig.android.owldroid.type.TriggerClaimChatInput
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toDeferred
import kotlinx.coroutines.Deferred

class ClaimsRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    private lateinit var claimsQuery: CommonClaimQuery

    fun fetchCommonClaimsAsync(): Deferred<Response<CommonClaimQuery.Data>> {
        claimsQuery = CommonClaimQuery(
            locale = defaultLocale(context)
        )

        return apolloClientWrapper.apolloClient.query(claimsQuery).toDeferred()
    }

    fun triggerClaimsChatAsync(claimTypeId: String?): Deferred<Response<TriggerClaimChatMutation.Data>> {
        val input = TriggerClaimChatInput(claimTypeId = Input.fromNullable(claimTypeId))
        val triggerClaimsChatMutation = TriggerClaimChatMutation(input)

        return apolloClientWrapper.apolloClient.mutate(triggerClaimsChatMutation).toDeferred()
    }

    fun triggerCallMeChatAsync() =
        apolloClientWrapper.apolloClient.mutate(TriggerCallMeChatMutation()).toDeferred()
}

