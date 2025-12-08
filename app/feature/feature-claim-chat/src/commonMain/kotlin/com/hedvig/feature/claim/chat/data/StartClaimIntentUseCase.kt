package com.hedvig.feature.claim.chat.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import octopus.ClaimIntentStartMutation

internal class StartClaimIntentUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(developmentFlow: Boolean): Either<ErrorMessage, ClaimIntent> {
    return either {
      apolloClient
        .mutation(
          ClaimIntentStartMutation(
            developmentFlow = Optional.present(developmentFlow),
          ),
        )
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .mapLeft {
          logcat { "StartClaimIntentUseCase error: $it" }
          ErrorMessage()
        }
        .bind()
        .claimIntentStart
        .toClaimIntent()
    }
  }
}
