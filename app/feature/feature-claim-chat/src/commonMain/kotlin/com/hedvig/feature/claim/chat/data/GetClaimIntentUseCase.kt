package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import octopus.ClaimIntentQuery

internal class GetClaimIntentUseCase(
  private val apolloClient: ApolloClient,
) {
  fun invoke(claimIntentId: ClaimIntentId): Flow<Either<ErrorMessage, TaskStepContent>> {
    return flow {
      var retries = 0
      while (currentCoroutineContext().isActive) {
        val claimIntentResult = either {
          apolloClient
            .query(ClaimIntentQuery(claimIntentId.value))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .safeExecute()
            .mapLeft(::ErrorMessage)
            .bind()
            .claimIntent
            .toClaimIntent()
        }

        when (claimIntentResult) {
          is Either.Left -> {
            if (retries++ > 5) {
              emit(claimIntentResult)
              break
            }
          }

          is Either.Right -> {
            val claimIntent = claimIntentResult.value
            val step = claimIntent.next.step
            val stepContent = step?.claimIntentStep?.stepContent
            if (step == null || stepContent !is StepContent.Task) {
              val errorMessage = "getClaimIntentUseCase returned a non-step result after observing an incomplete task"
              logcat(LogPriority.WARN) { errorMessage }
              emit(ErrorMessage(errorMessage).left())
              break
            }
            emit(TaskStepContent(step.claimIntentStep, stepContent).right())
            if (stepContent.isCompleted) {
              break
            }
          }
        }
        delay(POLLING_INTERVAL)
      }
    }
  }

  companion object {
    private const val POLLING_INTERVAL = 1_000L
  }
}

internal data class TaskStepContent(
  val step: ClaimIntentStep,
  val task: StepContent.Task,
)
