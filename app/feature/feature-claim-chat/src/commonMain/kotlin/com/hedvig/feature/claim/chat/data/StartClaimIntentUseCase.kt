package com.hedvig.feature.claim.chat.data

import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import octopus.ClaimIntentStartMutation

internal class StartClaimIntentUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(sourceMessageId: String?) = either {
    val data = apolloClient
      .mutation(ClaimIntentStartMutation(Optional.presentIfNotNull(sourceMessageId)))
      .safeExecute()
      .mapLeft(::ErrorMessage)
      .bind()
      .claimIntentStart
    ClaimIntent(
      id = data.id,
      step = data.currentStep.toClaimIntentStep(),
    )
  }
}

