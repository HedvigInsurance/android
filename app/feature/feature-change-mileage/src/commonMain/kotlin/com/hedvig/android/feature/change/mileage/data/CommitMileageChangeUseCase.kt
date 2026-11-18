package com.hedvig.android.feature.change.mileage.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import octopus.feature.changemileage.ChangeMileageCommitIntentMutation

internal interface CommitMileageChangeUseCase {
  suspend fun invoke(quoteId: String): Either<ErrorMessage, Unit>
}

@ContributesBinding(AppScope::class)
@Inject
internal class CommitMileageChangeUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CommitMileageChangeUseCase {
  override suspend fun invoke(quoteId: String): Either<ErrorMessage, Unit> = either {
    val userError = apolloClient
      .mutation(ChangeMileageCommitIntentMutation(quoteId))
      .safeExecute()
      .onLeft { logcat(LogPriority.WARN, it) { "Failed to commit change mileage quote $quoteId" } }
      .mapLeft { ErrorMessage() }
      .bind()
      .changeMileageCommitIntent
    ensure(userError == null) {
      logcat(LogPriority.WARN) { "Committing change mileage quote $quoteId returned a user error" }
      ErrorMessage(userError?.message)
    }
  }
}

// Always succeeds. To use it instead of the backend, uncomment it and move the @ContributesBinding here from
// CommitMileageChangeUseCaseImpl.
//
// @ContributesBinding(AppScope::class)
// @Inject
// internal class FakeCommitMileageChangeUseCase : CommitMileageChangeUseCase {
//   override suspend fun invoke(quoteId: String): Either<ErrorMessage, Unit> {
//     delay(1.seconds)
//     return Unit.right()
//   }
// }
