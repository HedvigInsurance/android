package com.hedvig.android.feature.change.mileage.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

internal interface CommitMileageChangeUseCase {
  suspend fun invoke(quoteId: String): Either<ErrorMessage, Unit>
}

// TODO(change-mileage): once the commit mutation is published, uncomment MutationChangeMileageCommitIntent.graphql
//  and this class, and move the @ContributesBinding here from FakeCommitMileageChangeUseCase, deleting the fake.
//
// @ContributesBinding(AppScope::class)
// @Inject
// internal class CommitMileageChangeUseCaseImpl(
//   private val apolloClient: ApolloClient,
// ) : CommitMileageChangeUseCase {
//   override suspend fun invoke(quoteId: String): Either<ErrorMessage, Unit> = either {
//     val result = apolloClient
//       .mutation(ChangeMileageCommitIntentMutation(quoteId))
//       .safeExecute()
//       .onLeft { logcat(LogPriority.WARN, it) { "Failed to commit change mileage quote $quoteId" } }
//       .mapLeft { ErrorMessage() }
//       .bind()
//     val userError = result.changeMileageCommitIntent.userError
//     ensure(userError == null) {
//       logcat(LogPriority.WARN) { "Committing change mileage quote $quoteId returned a user error" }
//       ErrorMessage(userError?.message)
//     }
//   }
// }

/** Stands in for the backend until the commit mutation is published. Always succeeds. */
@ContributesBinding(AppScope::class)
@Inject
internal class FakeCommitMileageChangeUseCase : CommitMileageChangeUseCase {
  override suspend fun invoke(quoteId: String): Either<ErrorMessage, Unit> {
    delay(1.seconds)
    return Unit.right()
  }
}
