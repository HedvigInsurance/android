package com.hedvig.android.apollo.auth.listeners

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.language.Language
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.MemberUpdateLanguageMutation

interface UploadLanguagePreferenceToBackendUseCase {
  suspend fun invoke(language: Language): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class UploadLanguagePreferenceToBackendUseCaseImpl(
  private val apolloClient: ApolloClient,
) : UploadLanguagePreferenceToBackendUseCase {
  override suspend fun invoke(language: Language): Either<ErrorMessage, Unit> {
    val ietfLanguageTag = language.toBcp47Format()
    @Suppress("ktlint:standard:max-line-length")
    return apolloClient
      .mutation(MemberUpdateLanguageMutation(ietfLanguageTag))
      .safeExecute()
      .fold(
        ifLeft = {
          logcat(LogPriority.WARN, it) {
            "UploadLanguagePreferenceToBackendUseCase: Failed to upload new language:$ietfLanguageTag to backend. Message:$it"
          }
          ErrorMessage().left()
        },
        ifRight = { response ->
          val userError = response.memberUpdateLanguage.userError
          if (userError != null) {
            logcat(LogPriority.ERROR) {
              "UploadLanguagePreferenceToBackendUseCase: Failed to upload new language:$ietfLanguageTag to backend. ErrorMessage:${userError.message}"
            }
            return ErrorMessage(userError.message).left()
          }
          val member = response.memberUpdateLanguage.member
          logcat {
            "UploadLanguagePreferenceToBackendUseCase: Language tag:$ietfLanguageTag successfully uploaded to backend for member id:${member?.id}. Responded language:${member?.language}"
          }
          Unit.right()
        },
      )
  }
}
