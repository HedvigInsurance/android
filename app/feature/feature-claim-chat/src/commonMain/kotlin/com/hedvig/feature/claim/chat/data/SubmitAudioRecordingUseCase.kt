package com.hedvig.feature.claim.chat.data

import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.ClaimIntentSubmitAudioMutation
import octopus.type.ClaimIntentSubmitAudioInput

internal class SubmitAudioRecordingUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(
    stepId: String,
    audioFileId: String?,
    freeText: String?,
  ) = either {
    val data = apolloClient
      .mutation(
        ClaimIntentSubmitAudioMutation(
          ClaimIntentSubmitAudioInput(
            stepId = stepId,
            audioFileId = Optional.presentIfNotNull(audioFileId),
            freeText = Optional.presentIfNotNull(freeText),
          ),
        ),
      )
      .safeExecute()
      .mapLeft(::ErrorMessage)
      .bind()
      .claimIntentSubmitAudio

    when {
      data.userError != null -> raise(ErrorMessage(data.userError.message))
      data.intent != null -> ClaimIntent(id = data.intent.id, step = data.intent.currentStep.toClaimIntentStep())
      else -> raise(ErrorMessage("No data"))
    }
  }
}
