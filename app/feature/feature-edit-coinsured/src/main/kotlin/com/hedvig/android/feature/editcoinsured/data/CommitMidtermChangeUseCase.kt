package com.hedvig.android.feature.editcoinsured.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.appreview.SelfServiceCompletedEventManager
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.datetime.LocalDate
import octopus.CommitMidtermChangeMutation
import octopus.type.MidtermChangeIntentState

internal interface CommitMidtermChangeUseCase {
  suspend fun invoke(intentId: String): Either<ErrorMessage, CommitMidtermChangeSuccess>
}

internal class CommitMidtermChangeUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val networkCacheManager: NetworkCacheManager,
  private val selfServiceCompletedEventManager: SelfServiceCompletedEventManager,
) : CommitMidtermChangeUseCase {
  override suspend fun invoke(intentId: String): Either<ErrorMessage, CommitMidtermChangeSuccess> = either {
    val result = apolloClient.mutation(CommitMidtermChangeMutation(intentId))
      .safeExecute(::ErrorMessage)
      .bind()

    val userError = result.midtermChangeIntentCommit.userError
    if (userError != null) {
      raise(ErrorMessage(userError.message))
    }

    when (val state = result.midtermChangeIntentCommit.intent?.state) {
      MidtermChangeIntentState.COMPLETED -> {
        networkCacheManager.clearCache()
        selfServiceCompletedEventManager.completedSelfServiceSuccessfully()
        CommitMidtermChangeSuccess(
          result.midtermChangeIntentCommit.intent.activationDate,
        )
      }
      MidtermChangeIntentState.INITIATED,
      MidtermChangeIntentState.UNKNOWN__,
      null,
      -> raise(ErrorMessage("Could not commit, state was: ${state?.name}"))
    }
  }
}

internal data class CommitMidtermChangeSuccess(
  val contractUpdateDate: LocalDate,
)
