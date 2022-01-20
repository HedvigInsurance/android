package com.hedvig.app.feature.chat.usecase

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.rightIfNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.TriggerFreeTextChatMutation
import com.hedvig.app.util.apollo.safeQuery

class TriggerFreeTextChatUseCase(
    private val apolloClient: ApolloClient,
) {
    suspend operator fun invoke(): Either<FreeTextError, FreeTextSuccess> {
        return apolloClient
            .mutate(TriggerFreeTextChatMutation())
            .safeQuery()
            .toEither { FreeTextError.NetworkError }
            .flatMap { data ->
                data.triggerFreeTextChat
                    .rightIfNotNull { FreeTextError.CouldNotTrigger }
                    .map { FreeTextSuccess }
            }
    }
}

sealed class FreeTextError {
    object NetworkError : FreeTextError()
    object CouldNotTrigger : FreeTextError()
}

object FreeTextSuccess
