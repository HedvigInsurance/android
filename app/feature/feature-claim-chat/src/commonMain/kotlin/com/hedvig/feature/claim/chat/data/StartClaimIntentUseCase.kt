package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.ClaimIntentStartMutation
import octopus.fragment.ClaimIntentFragment

internal class StartClaimIntentUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(sourceMessageId: String?): Either<ErrorMessage, ClaimIntent> {
    return either {
      apolloClient
        .mutation(ClaimIntentStartMutation(Optional.presentIfNotNull(sourceMessageId)))
        .safeExecute()
        .mapLeft(::ErrorMessage)
        .bind()
        .claimIntentStart
        .toClaimIntent()
    }
  }
}

