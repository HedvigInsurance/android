package com.hedvig.feature.claim.chat.data

import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import octopus.ClaimIntentQuery

internal class GetClaimIntentUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(claimIntentId: String) = either {
    val data = apolloClient
      .query(ClaimIntentQuery(claimIntentId))
      .safeExecute()
      .mapLeft(::ErrorMessage)
      .bind()
      .claimIntent

    ClaimIntent(
      id = data.id,
      step = data.currentStep.toClaimIntentStep(),
    )
  }
}
