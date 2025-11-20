package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.ClaimIntentSubmitTaskMutation

internal class SubmitTaskUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(stepId: String): Either<ErrorMessage, ClaimIntent> {
    return either {
      apolloClient
        .mutation(ClaimIntentSubmitTaskMutation(stepId = stepId))
        .safeExecute()
        .mapLeft(::ErrorMessage)
        .bind()
        .claimIntentSubmitTask
        .toClaimIntent()
    }
  }
}
