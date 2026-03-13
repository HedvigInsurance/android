package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.CommonFile
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import octopus.ClaimIntentSubmitAudioMutation
import octopus.type.ClaimIntentSubmitAudioInput

internal class SubmitAudioRecordingUseCase(
  private val apolloClient: ApolloClient,
  private val uploadFileUseCase: UploadFileUseCase,
  private val languageService: LanguageService,
) {
  suspend fun invoke(stepId: StepId, freeText: String): Either<ClaimChatErrorMessage, ClaimIntent> {
    return either { invoke(stepId, null, freeText) }
  }

  suspend fun invoke(stepId: StepId, commonFile: CommonFile, uploadUrl: String): Either<ClaimChatErrorMessage, ClaimIntent> {
    return either {
      val uploadResult = either {
        uploadFileUseCase.invoke(commonFile, uploadUrl)
      }.mapLeft { ClaimChatErrorMessage.GeneralError }
        .bind()
      val fileId = uploadResult.fileId
      logcat { "SubmitFileUploadUseCase uploaded file with Uri:${commonFile.fileName} got back fileId:$fileId" }
      invoke(stepId, fileId, null)
    }
  }

  context(_: Raise<ClaimChatErrorMessage>)
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
      .mapLeft {
        logcat { "SubmitAudioRecordingUseCase error: $it" }
        ClaimChatErrorMessage.GeneralError
      }
      .bind()
      .claimIntentSubmitAudio
      .toClaimIntent(languageService.getLocale())
  }
}
