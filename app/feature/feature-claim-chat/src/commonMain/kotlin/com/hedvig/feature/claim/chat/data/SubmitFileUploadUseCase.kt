package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.eygraber.uri.Uri
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import com.hedvig.feature.claim.chat.data.file.FileService
import kotlin.jvm.JvmInline
import octopus.ClaimIntentSubmitFileUploadMutation
import octopus.type.ClaimIntentSubmitFileUploadInput

@JvmInline
internal value class CommonFileId(val value: String)

internal class SubmitFileUploadUseCase(
  private val apolloClient: ApolloClient,
  private val uploadFileUseCase: UploadFileUseCase,
  private val fileService: FileService,
) {
  suspend fun invoke(stepId: StepId, fileUris: List<Uri>, uploadUrl: String): Either<ErrorMessage, ClaimIntent> {
    return either {
      val commonFiles = fileUris.map { fileUri ->
        fileService.convertToCommonFile(fileUri)
      }
      val fileIds = buildList {
        // todo!!!
        commonFiles.forEach {
          add(
            uploadFileUseCase.invoke(
              it,
              uploadUrl,
            ).fileId,
          )
        }
      }
      logcat { "SubmitFileUploadUseCase uploaded file with Uris:$fileUris got back fileIds:$fileIds" }
      invoke(stepId, fileIds)
    }
  }

  context(_: Raise<ErrorMessage>)
  private suspend fun invoke(stepId: StepId, commonFileIds: List<CommonFileId>): ClaimIntent {
    return apolloClient
      .mutation(
        ClaimIntentSubmitFileUploadMutation(
          ClaimIntentSubmitFileUploadInput(
            stepId = stepId.value,
            fileIds = commonFileIds.map {
              it.value
            },
          ),
        ),
      )
      .safeExecute()
      .mapLeft {
        logcat { "SubmitFileUploadUseCase error: $it" }
        ErrorMessage()
      }
      .bind()
      .claimIntentSubmitFileUpload
      .toClaimIntent()
  }
}
