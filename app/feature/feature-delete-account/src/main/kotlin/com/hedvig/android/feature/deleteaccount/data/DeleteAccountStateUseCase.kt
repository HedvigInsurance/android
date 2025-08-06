package com.hedvig.android.feature.deleteaccount.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import octopus.DeleteAccountStateQuery
import octopus.type.ClaimStatus

internal class DeleteAccountStateUseCase(
  private val apolloClient: ApolloClient,
  private val deleteAccountRequestStorage: DeleteAccountRequestStorage,
  private val featureManager: FeatureManager,
) {
  suspend fun invoke(): Flow<DeleteAccountState> {
    return combine(
      deleteAccountRequestStorage.hasRequestedTermination(),
      featureManager.isFeatureEnabled(Feature.ENABLE_CLAIM_HISTORY).flatMapLatest { enableClaimHistory ->
        apolloClient.query(
          DeleteAccountStateQuery(enableClaimHistory),
        ).fetchPolicy(FetchPolicy.CacheAndNetwork).safeFlow {
          DeleteAccountState.NetworkError
        }
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
        currentMember.claims?.map { it.status }
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
