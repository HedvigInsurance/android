package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import octopus.ClaimIntentSubmitSelectMutation
import octopus.type.ClaimIntentSubmitSelectInput

internal class SubmitSelectUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun invoke(id: StepId, selectedId: String): Either<ErrorMessage, ClaimIntent> {
    return either {
      val data = apolloClient
        .mutation(
          ClaimIntentSubmitSelectMutation(
            input = ClaimIntentSubmitSelectInput(
              stepId = id.value,
              selectedId = selectedId,
            ),
          ),
        )
        .safeExecute()
        .mapLeft {
          logcat { "SubmitSelectUseCase error: $it" }
          ErrorMessage()
        }
        .bind()
        .claimIntentSubmitSelect

      when {
        data.userError != null -> raise(ErrorMessage(data.userError.message))
        data.intent != null -> data.intent.toClaimIntent(languageService.getLocale())
        else -> raise(ErrorMessage())
      }
    }
  }
}
