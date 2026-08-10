package com.hedvig.android.data.claimintent

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.ResumableClaimIntentQuery

interface GetResumableClaimIntentUseCase {
  /**
   * A null right side means the member has no resumable draft claim.
   */
  suspend fun invoke(): Either<ErrorMessage, ResumableClaimIntent?>
}

@Inject
@ContributesBinding(AppScope::class)
internal class GetResumableClaimIntentUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetResumableClaimIntentUseCase {
  override suspend fun invoke(): Either<ErrorMessage, ResumableClaimIntent?> {
    return either {
      apolloClient
        .query(ResumableClaimIntentQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .mapLeft { error ->
          logcat(operationError = error) { "GetResumableClaimIntentUseCase failed with $error" }
          ErrorMessage()
        }
        .bind()
        .currentMember
        .resumableClaimIntent
        ?.let { resumableClaimIntent ->
          ResumableClaimIntent(
            id = resumableClaimIntent.id,
            displayName = resumableClaimIntent.displayName,
            startedAt = resumableClaimIntent.createdAt,
          )
        }
    }
  }
}
