package com.hedvig.feature.claim.chat.data

import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.ClaimIntentSubmitTaskMutation

internal class SubmitTaskUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(stepId: String) = either {
    val data = apolloClient
      .mutation(ClaimIntentSubmitTaskMutation(stepId = stepId))
      .safeExecute()
      .mapLeft(::ErrorMessage)
      .bind()
      .claimIntentSubmitTask

    when {
      data.userError != null -> raise(ErrorMessage(data.userError.message))
      data.intent != null -> ClaimIntent(id = data.intent.id, step = data.intent.currentStep.toClaimIntentStep())
      else -> raise(ErrorMessage("No data"))
    }
  }
}
