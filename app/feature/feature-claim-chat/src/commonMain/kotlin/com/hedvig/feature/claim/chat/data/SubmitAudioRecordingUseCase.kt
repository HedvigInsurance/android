package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.ClaimIntentSubmitAudioMutation
import octopus.fragment.ClaimIntentMutationOutputFragment
import octopus.type.ClaimIntentSubmitAudioInput

internal class SubmitAudioRecordingUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    stepId: String,
    audioFileId: String?,
    freeText: String?,
  ): Either<ErrorMessage, ClaimIntent> {
    return either {
      apolloClient
        .mutation(
          ClaimIntentSubmitAudioMutation(
            ClaimIntentSubmitAudioInput(
              stepId = stepId,
              audioFileId = Optional.presentIfNotNull(audioFileId), // for testing "271788f5-07d0-4394-a814-001ef2e3d128"
              freeText = Optional.presentIfNotNull(freeText),
            ),
          ),
        )
        .safeExecute()
        .mapLeft(::ErrorMessage)
        .bind()
        .claimIntentSubmitAudio
        .toClaimIntent()
    }
  }
}
