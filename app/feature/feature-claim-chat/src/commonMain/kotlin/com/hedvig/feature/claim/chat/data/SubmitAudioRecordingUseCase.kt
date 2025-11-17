package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import kotlin.jvm.JvmInline
import octopus.ClaimIntentSubmitAudioMutation
import octopus.fragment.ClaimIntentMutationOutputFragment
import octopus.type.ClaimIntentSubmitAudioInput

@JvmInline
internal value class AudioFileId(val value: String)

internal class SubmitAudioRecordingUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    stepId: StepId,
    freeText: String,
  ): Either<ErrorMessage, ClaimIntent> {
    return invoke(stepId, null, freeText)
  }

  suspend fun invoke(
    stepId: StepId,
    audioFileId: AudioFileId,
  ): Either<ErrorMessage, ClaimIntent> {
    return invoke(stepId, audioFileId, null)
  }

  private suspend fun invoke(
    stepId: StepId,
    audioFileId: AudioFileId?,
    freeText: String?,
  ): Either<ErrorMessage, ClaimIntent> {
    return either {
      apolloClient
        .mutation(
          ClaimIntentSubmitAudioMutation(
            ClaimIntentSubmitAudioInput(
              stepId = stepId.value,
              audioFileId = Optional.presentIfNotNull(audioFileId?.value), // for testing "271788f5-07d0-4394-a814-001ef2e3d128"
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
