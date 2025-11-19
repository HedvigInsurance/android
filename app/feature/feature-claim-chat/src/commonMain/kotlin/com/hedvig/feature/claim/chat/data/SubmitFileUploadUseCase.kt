package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.eygraber.uri.Uri
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import com.hedvig.feature.claim.chat.data.file.FileService
import kotlin.jvm.JvmInline
import octopus.ClaimIntentSubmitAudioMutation
import octopus.type.ClaimIntentSubmitAudioInput

@JvmInline
internal value class CommonFileId(val value: String)

internal class SubmitFileUploadUseCase(
  private val apolloClient: ApolloClient,
  private val uploadFileUseCase: UploadFileUseCase,
  private val fileService: FileService,
) {
  suspend fun invoke(
    stepId: StepId,
    freeText: String,
  ): Either<ErrorMessage, ClaimIntent> {
    return either { invoke(stepId, null, freeText) }
  }

  suspend fun invoke(
    stepId: StepId,
    fileUri: Uri,
    uploadUrl: String,
  ): Either<ErrorMessage, ClaimIntent> {
    return either {
      val fileId = uploadFileUseCase.invoke(
        fileService.convertToCommonFile(fileUri),
        uploadUrl
      ).fileId
      logcat { "SubmitFileUploadUseCase uploaded file with Uri:$fileUri got back fileId:$fileUri" }
      invoke(stepId, fileId, null)
    }
  }

  context(_: Raise<ErrorMessage>)
  private suspend fun invoke(
    stepId: StepId,
    commonFileId: CommonFileId?,
    freeText: String?,
  ): ClaimIntent {
    return apolloClient
      .mutation(
        ClaimIntentSubmitAudioMutation(
          ClaimIntentSubmitAudioInput(
            stepId = stepId.value,
            audioFileId = Optional.presentIfNotNull(commonFileId?.value), // for testing "271788f5-07d0-4394-a814-001ef2e3d128"
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
