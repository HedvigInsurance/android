package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import octopus.ClaimIntentStartMutation
import octopus.type.ClaimIntentStartInput

internal class StartClaimIntentUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    sourceMessageId: String?,
    developmentFlow: Boolean,
  ): Either<ErrorMessage, ClaimIntent> = either {
    val data = apolloClient
      .mutation(
        ClaimIntentStartMutation(
          Optional.presentIfNotNull(
            ClaimIntentStartInput(
              sourceMessageId = Optional.presentIfNotNull(
                sourceMessageId,
              ),
              developmentFlow = Optional.presentIfNotNull(developmentFlow),
            ),
          ),
        ),
      )
      .safeExecute()
      .onLeft {
        logcat { "Mariia: StartClaimIntentUseCase: $it" }
      }
      .mapLeft(::ErrorMessage)
      .bind()
      .claimIntentStart
    ClaimIntent(
      id = data.id,
      step = data.currentStep.toClaimIntentStep(),
    )
  }
}

