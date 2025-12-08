package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import octopus.ClaimIntentRegretStepMutation

internal interface RegretStepUseCase {
  suspend fun invoke(id: StepId): Either<ErrorMessage, ClaimIntent>
}

internal class RegretStepUseCaseImpl(
  private val apolloClient: ApolloClient,
) : RegretStepUseCase {
  override suspend fun invoke(id: StepId): Either<ErrorMessage, ClaimIntent> = either {
    val data = apolloClient
      .mutation(
        ClaimIntentRegretStepMutation(
          stepId = id.value,
        ),
      )
      .safeExecute()
      .mapLeft {
        logcat { "SkipStepUseCase error: $it" }
        ErrorMessage()
      }
      .bind()
      .claimIntentRegretStep

    when {
      data.userError != null -> raise(ErrorMessage(data.userError.message))
      data.intent != null -> data.intent.toClaimIntent()
      else -> raise(ErrorMessage("No data"))
    }
  }
}
