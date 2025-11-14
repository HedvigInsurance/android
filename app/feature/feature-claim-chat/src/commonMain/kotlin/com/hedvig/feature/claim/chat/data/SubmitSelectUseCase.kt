package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.ClaimIntentSubmitSelectMutation
import octopus.type.ClaimIntentSubmitSelectInput

internal class SubmitSelectUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(stepId: String, selectedId: String): Either<ErrorMessage, ClaimIntent> {
    return either {
      val data = apolloClient
        .mutation(
          ClaimIntentSubmitSelectMutation(
            input = ClaimIntentSubmitSelectInput(
              stepId = stepId,
              selectedId = selectedId,
            ),
          ),
        )
        .safeExecute()
        .mapLeft(::ErrorMessage)
        .bind()
        .claimIntentSubmitSelect

      when {
        data.userError != null -> raise(ErrorMessage(data.userError.message))
        data.intent != null -> data.intent.toClaimIntent()
        else -> raise(ErrorMessage("No data"))
      }
    }
  }
}
