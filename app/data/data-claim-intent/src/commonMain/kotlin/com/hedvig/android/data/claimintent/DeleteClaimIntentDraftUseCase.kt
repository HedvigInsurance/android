package com.hedvig.android.data.claimintent

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.ClaimIntentDeleteDraftMutation

interface DeleteClaimIntentDraftUseCase {
  suspend fun invoke(id: String): Either<ErrorMessage, Unit>
}

@Inject
@ContributesBinding(AppScope::class)
internal class DeleteClaimIntentDraftUseCaseImpl(
  private val apolloClient: ApolloClient,
) : DeleteClaimIntentDraftUseCase {
  override suspend fun invoke(id: String): Either<ErrorMessage, Unit> {
    return either {
      val deleted = apolloClient
        .mutation(ClaimIntentDeleteDraftMutation(id))
        .safeExecute()
        .mapLeft { error ->
          logcat(operationError = error) { "DeleteClaimIntentDraftUseCase failed with $error" }
          ErrorMessage()
        }
        .bind()
        .claimIntentDeleteDraft
      ensure(deleted) { ErrorMessage() }
    }
  }
}
