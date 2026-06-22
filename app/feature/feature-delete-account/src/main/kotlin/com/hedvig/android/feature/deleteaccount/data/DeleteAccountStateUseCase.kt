package com.hedvig.android.feature.deleteaccount.data

import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import octopus.DeleteAccountStateQuery
import octopus.type.ClaimStatus

@SingleIn(AppScope::class)
@Inject
internal class DeleteAccountStateUseCase(
  private val apolloClient: ApolloClient,
  private val deleteAccountRequestStorage: DeleteAccountRequestStorage,
) {
  suspend fun invoke(): Flow<DeleteAccountState> {
    return combine(
      deleteAccountRequestStorage.hasRequestedTermination(),
      apolloClient.query(
        DeleteAccountStateQuery(true),
      ).fetchPolicy(FetchPolicy.CacheAndNetwork).safeFlow {
        DeleteAccountState.NetworkError
      },
    ) { hasRequestedTermination, queryResponse ->
      if (hasRequestedTermination) {
        return@combine DeleteAccountState.AlreadyRequestedDeletion
      }
      val currentMember = queryResponse.getOrNull()?.currentMember ?: return@combine DeleteAccountState.NetworkError
      val hasActiveContracts = currentMember.activeContracts.isNotEmpty()
      if (hasActiveContracts) {
        return@combine DeleteAccountState.HasActiveInsurance
      }
      val hasActiveClaims = (
        currentMember.claims?.toNonEmptyListOrNull()?.map { it.status }
          ?: currentMember.claimsActive.orEmpty().map { it.status }
      ).any { status ->
        // If we do not explicitly know that a claim is closed, we err on the side of caution and assume it's open
        when (status) {
          ClaimStatus.CREATED,
          ClaimStatus.IN_PROGRESS,
          ClaimStatus.REOPENED,
          ClaimStatus.UNKNOWN__,
          null,
          -> true

          ClaimStatus.CLOSED -> false
        }
      }
      if (hasActiveClaims) {
        return@combine DeleteAccountState.HasOngoingClaim
      }
      DeleteAccountState.CanDelete
    }
  }
}

internal sealed interface DeleteAccountState {
  data object NetworkError : DeleteAccountState

  data object AlreadyRequestedDeletion : DeleteAccountState

  data object HasOngoingClaim : DeleteAccountState

  data object HasActiveInsurance : DeleteAccountState

  data object CanDelete : DeleteAccountState
}
