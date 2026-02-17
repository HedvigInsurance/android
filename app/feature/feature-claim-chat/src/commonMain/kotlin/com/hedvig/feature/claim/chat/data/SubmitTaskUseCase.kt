package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import octopus.ClaimIntentSubmitTaskMutation

internal class SubmitTaskUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun invoke(stepId: String): Either<ErrorMessage, ClaimIntent> {
    val maxAttempts = 6
    repeat(maxAttempts) { attempt ->
      val result = either {
        apolloClient
          .mutation(ClaimIntentSubmitTaskMutation(stepId = stepId))
          .safeExecute()
          .mapLeft {
            logcat(LogPriority.WARN) { "SubmitTaskUseCase error (attempt ${attempt + 1}/$maxAttempts): $it" }
            ErrorMessage()
          }
          .bind()
          .claimIntentSubmitTask
          .toClaimIntent(languageService.getLocale())
      }
      if (result.isRight()) {
        return result
      }
      val delay = (100 * (2.0.pow(attempt))).milliseconds
      logcat(LogPriority.INFO) { "SubmitTaskUseCase retrying in $delay" }
      delay(delay)
    }

    logcat(LogPriority.ERROR) { "SubmitTaskUseCase failed after $maxAttempts attempts" }
    return ErrorMessage().left()
  }
}
