package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.feature.claim.chat.data.file.AudioFileReference
import kotlin.jvm.JvmInline
import octopus.ClaimIntentSubmitAudioMutation
import octopus.type.ClaimIntentSubmitAudioInput

@JvmInline
internal value class AudioFileId(val value: String)

internal class SubmitAudioRecordingUseCase(
  private val uploadAudioUseCase: UploadAudioUseCase,
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    stepId: StepId,
    freeText: String,
  ): Either<ErrorMessage, ClaimIntent> {
    return either { invoke(stepId, null, freeText) }
  }

  suspend fun invoke(
    stepId: StepId,
    audioFileReference: AudioFileReference,
    uploadUrl: String,
  ): Either<ErrorMessage, ClaimIntent> {
    return either {
      val audioFileId = uploadAudioUseCase.invoke(audioFileReference, uploadUrl).fileId
      invoke(stepId, audioFileId, null)
    }
  }

  context(_: Raise<ErrorMessage>)
  private suspend fun invoke(
    stepId: StepId,
    audioFileId: AudioFileId?,
    freeText: String?,
  ): ClaimIntent {
    return apolloClient
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
