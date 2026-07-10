package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.ClaimIntentSubmitInformationMutation
import octopus.type.ClaimIntentSubmitInformationInput

@SingleIn(AppScope::class)
@Inject
internal class SubmitInformationUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun invoke(id: StepId): Either<ClaimChatErrorMessage, ClaimIntent> {
    return either {
      apolloClient
        .mutation(
          ClaimIntentSubmitInformationMutation(
            input = ClaimIntentSubmitInformationInput(
              stepId = id.value,
            ),
          ),
        )
        .safeExecute()
        .mapLeft {
          logcat { "SubmitInformationUseCase error: $it" }
          ClaimChatErrorMessage.GeneralError
        }
        .bind()
        .claimIntentSubmitInformation
        .toClaimIntent(languageService.getLocale())
    }
  }
}
