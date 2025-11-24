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
import com.hedvig.android.logger.logcat
import com.hedvig.feature.claim.chat.data.file.CommonFile
import octopus.ClaimIntentSubmitAudioMutation
import octopus.type.ClaimIntentSubmitAudioInput

internal class SubmitAudioRecordingUseCase(
  private val apolloClient: ApolloClient,
  private val uploadFileUseCase: UploadFileUseCase,
) {
  suspend fun invoke(stepId: StepId, freeText: String): Either<ErrorMessage, ClaimIntent> {
    return either { invoke(stepId, null, freeText) }
  }

  suspend fun invoke(stepId: StepId, commonFile: CommonFile, uploadUrl: String): Either<ErrorMessage, ClaimIntent> {
    return either {
      val fileId = uploadFileUseCase.invoke(commonFile, uploadUrl).fileId
      logcat { "SubmitFileUploadUseCase uploaded file with Uri:${commonFile.fileName} got back fileId:$fileId" }
      invoke(stepId, fileId, null)
    }
  }

  context(_: Raise<ErrorMessage>)
  private suspend fun invoke(stepId: StepId, commonFileId: CommonFileId?, freeText: String?): ClaimIntent {
    return apolloClient
      .mutation(
        ClaimIntentSubmitAudioMutation(
          ClaimIntentSubmitAudioInput(
            stepId = stepId.value,
            audioFileId = Optional.presentIfNotNull(commonFileId?.value),
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
