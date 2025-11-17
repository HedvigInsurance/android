package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import octopus.ClaimIntentQuery

internal class GetClaimIntentUseCase(
  private val apolloClient: ApolloClient,
) {
  fun invoke(claimIntentId: ClaimIntentId): Flow<Either<ErrorMessage, ClaimIntent>> {
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
          is Either.Left<ErrorMessage> -> {
            if (retries++ > 5) {
              emit(claimIntentResult)
              break
            }
          }

          is Either.Right<ClaimIntent> -> {
            val claimIntent = claimIntentResult.value
            val stepContent = claimIntent.step.stepContent
            emit(claimIntent.right())
            if (stepContent !is StepContent.Task || stepContent.isCompleted) {
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
