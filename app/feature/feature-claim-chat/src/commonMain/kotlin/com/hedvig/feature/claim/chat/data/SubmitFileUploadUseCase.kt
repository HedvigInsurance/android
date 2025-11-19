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
import octopus.ClaimIntentSubmitFileUploadMutation
import octopus.type.ClaimIntentSubmitAudioInput
import octopus.type.ClaimIntentSubmitFileUploadInput

@JvmInline
internal value class CommonFileId(val value: String)

internal class SubmitFileUploadUseCase(
  private val apolloClient: ApolloClient,
  private val uploadFileUseCase: UploadFileUseCase,
  private val fileService: FileService,
) {
  suspend fun invoke(
    stepId: StepId,
    fileUri: Uri,
    uploadUrl: String,
  ): Either<ErrorMessage, ClaimIntent> {
    return either {
      val commonFile = fileService.convertToCommonFile(fileUri)
      val fileId = uploadFileUseCase.invoke(commonFile, uploadUrl).fileId
      logcat { "SubmitFileUploadUseCase uploaded file with Uri:$fileUri got back fileId:$fileId" }
      invoke(stepId, fileId)
    }
  }

  context(_: Raise<ErrorMessage>)
  private suspend fun invoke(
    stepId: StepId,
    commonFileId: CommonFileId?,
  ): ClaimIntent {
    return apolloClient
      .mutation(
        ClaimIntentSubmitFileUploadMutation(
          ClaimIntentSubmitFileUploadInput(
            stepId = stepId.value,
            fileIds = listOfNotNull(commonFileId?.value)
          ),
        ),
      )
      .safeExecute()
      .mapLeft(::ErrorMessage)
      .bind()
      .claimIntentSubmitFileUpload
      .toClaimIntent()
  }
}
