package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.apollo.ErrorMessage
import octopus.ClaimIntentSkipStepMutation

internal interface SkipStepUseCase {
  suspend fun invoke(id: StepId): Either<ErrorMessage, ClaimIntent>
}

internal class SkipStepUseCaseImpl(
  private val apolloClient: ApolloClient,
): SkipStepUseCase {
  override suspend fun invoke(id: StepId): Either<ErrorMessage, ClaimIntent> = either {
    val data = apolloClient
      .mutation(
        ClaimIntentSkipStepMutation(
          stepId = id.value,
      ),
    )
    .safeExecute()
      .mapLeft(::ErrorMessage)
      .bind()
      .claimIntentSkipStep
    when {
      data.userError != null -> raise(ErrorMessage(data.userError.message))
      data.intent != null -> data.intent.toClaimIntent()
      else -> raise(ErrorMessage("No data"))
    }
  }
}
