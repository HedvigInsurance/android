package com.hedvig.android.feature.deleteaccount.data

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.first
import octopus.MemberDeletionRequestMutation

internal class RequestAccountDeletionUseCase(
  private val apolloClient: ApolloClient,
  private val memberIdService: MemberIdService,
  private val deleteAccountRequestStorage: DeleteAccountRequestStorage,
) {
  suspend fun invoke(): Either<RequestAccountError, Unit> {
    return either {
      val memberId = memberIdService.getMemberId().first()
      if (memberId == null) {
        logcat(LogPriority.ERROR) {
          "Tried to request account deletion with a null memberId. " +
            "This should not be possible as they should be redirected to the login screen without a member ID"
        }
        raise(RequestAccountError.NoMemberIdFound)
      }
      val userErrorMessage = apolloClient.mutation(MemberDeletionRequestMutation())
        .safeExecute()
        .toEither { message, throwable ->
          logcat(LogPriority.WARN, throwable) {
            "RequestAccountDeletionUseCase failed to request account deletion. Message:$message."
          }
          RequestAccountError.NetworkError(message, throwable)
        }
        .bind()
        .memberDeletionRequest
        ?.message
      if (userErrorMessage != null) {
        raise(RequestAccountError.NetworkError(userErrorMessage, null))
      }
      catch({ deleteAccountRequestStorage.storeTerminationRequest(memberId) }) {
        raise(RequestAccountError.FailedToPersistDeletionRequest)
      }
      Unit
    }
  }
}

internal sealed interface RequestAccountError {
  data class NetworkError(val message: String?, val throwable: Throwable?) : RequestAccountError

  data object FailedToPersistDeletionRequest : RequestAccountError

  data object NoMemberIdFound : RequestAccountError
}
