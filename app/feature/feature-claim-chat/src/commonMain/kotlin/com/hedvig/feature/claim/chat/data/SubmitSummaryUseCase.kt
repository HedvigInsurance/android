package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.ClaimIntentSubmitSummaryMutation

internal class SubmitSummaryUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(stepId: String): Either<ErrorMessage, ClaimIntent> {
    return either {
      apolloClient
        .mutation(ClaimIntentSubmitSummaryMutation(stepId = stepId))
        .safeExecute()
        .mapLeft(::ErrorMessage)
        .bind()
        .claimIntentSubmitSummary
        .toClaimIntent()
    }
  }
}
