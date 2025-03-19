package com.hedvig.android.data.cross.sell.after.claim.closed

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import octopus.ClaimAcknowledgeClosedStatusMutation

interface CrossSellAfterClaimClosedRepository {
  fun shouldShowCrossSellAfterClaim(): Flow<Boolean>

  suspend fun acknowledgeClaimClosedStatus(claimId: String): Either<ErrorMessage, Unit>

  suspend fun showedCrossSellAfterClaim()
}

class CrossSellAfterClaimClosedRepositoryImpl(
  private val apolloClient: ApolloClient,
) : CrossSellAfterClaimClosedRepository {
  /**
   * Purposefully not stored in persistent storage so that if the app is killed after this was set, we do not still
   * show the cross sells in the home screen.
   */
  private val shouldShowCrossSellAfterClaim = MutableStateFlow(false)

  override fun shouldShowCrossSellAfterClaim(): Flow<Boolean> = shouldShowCrossSellAfterClaim

  override suspend fun acknowledgeClaimClosedStatus(claimId: String): Either<ErrorMessage, Unit> {
    return apolloClient
      .mutation(ClaimAcknowledgeClosedStatusMutation(claimId))
      .safeExecute(::ErrorMessage)
      .map { response ->
        either {
          val userError = response.claimAcknowledgeClosedStatus?.userError
          if (userError != null) {
            logcat(LogPriority.ERROR) { "ClaimAcknowledgeClosedStatusMutation failed: ${userError.message}" }
            raise(ErrorMessage(userError.message))
          }
          shouldShowCrossSellAfterClaim.value = true
          Unit
        }
      }
  }

  override suspend fun showedCrossSellAfterClaim() {
    shouldShowCrossSellAfterClaim.value = false
  }
}
