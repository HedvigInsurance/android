package com.hedvig.android.apollo.auth.listeners

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.MemberUpdateLanguageMutation

interface UploadLanguagePreferenceToBackendUseCase {
  suspend fun invoke()
}

internal class UploadLanguagePreferenceToBackendUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) : UploadLanguagePreferenceToBackendUseCase {
  override suspend fun invoke() {
    val ietfLanguageTag = languageService.getLanguage().toBcp47Format()
    @Suppress("ktlint:standard:max-line-length")
    apolloClient
      .mutation(MemberUpdateLanguageMutation(ietfLanguageTag))
      .safeExecute()
      .toEither()
      .fold(
        ifLeft = {
          logcat(LogPriority.WARN, it.throwable) {
            "UploadLanguagePreferenceToBackendUseCase: Failed to upload new language:$ietfLanguageTag to backend. Message:${it.message}"
          }
        },
        ifRight = { response ->
          if (response.memberUpdateLanguage.userError != null) {
            logcat(LogPriority.ERROR) {
              "UploadLanguagePreferenceToBackendUseCase: Failed to upload new language:$ietfLanguageTag to backend. ErrorMessage:${response.memberUpdateLanguage.userError.message}"
            }
          }
          val member = response.memberUpdateLanguage.member
          logcat {
            "UploadLanguagePreferenceToBackendUseCase: Language tag:$ietfLanguageTag successfully uploaded to backend for member id:${member?.id}. Responded language:${member?.language}"
          }
        },
      )
  }
}
