package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import octopus.ClaimIntentQuery

internal class GetClaimIntentUseCase(
  private val apolloClient: ApolloClient,
) {
  private val POLLING_INTERVAL = 1.seconds

  fun invoke(claimIntentId: String): Flow<Either<ErrorMessage, ClaimIntent>> = flow {
    while (true) {
      val result = apolloClient
        .query(ClaimIntentQuery(claimIntentId))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .mapLeft(::ErrorMessage)

      val domainResult = result.map { data ->
        ClaimIntent(
          id = data.claimIntent.id,
          step = data.claimIntent.currentStep.toClaimIntentStep(),
        )
      }

      emit(domainResult)

      delay(POLLING_INTERVAL)
    }
  }
}
