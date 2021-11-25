package com.hedvig.app.feature.chat.usecase

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.TriggerFreeTextChatMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import e

class TriggerFreeTextChatUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke() {
        val result = apolloClient
            .mutate(TriggerFreeTextChatMutation())
            .safeQuery()

        if (result is QueryResult.Error) {
            result.message?.let { e { it } }
        }
    }
}
